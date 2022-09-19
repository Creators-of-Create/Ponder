package net.createmod.ponder.foundation.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;

import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperByteBufferCache.Compartment;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.outliner.AABBOutline;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderWorld;
import net.createmod.ponder.foundation.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldSectionElement extends AnimatedSceneElement {

	public static final Compartment<Pair<Integer, Integer>> PONDER_WORLD_SECTION = new Compartment<>();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	List<BlockEntity> renderedTileEntities;
	List<Pair<BlockEntity, Consumer<Level>>> tickableTileEntities;
	Selection section;
	boolean redraw;

	Vec3 prevAnimatedOffset = Vec3.ZERO;
	Vec3 animatedOffset = Vec3.ZERO;
	Vec3 prevAnimatedRotation = Vec3.ZERO;
	Vec3 animatedRotation = Vec3.ZERO;
	Vec3 centerOfRotation = Vec3.ZERO;
	Vec3 stabilizationAnchor = null;

	BlockPos selectedBlock;

	public WorldSectionElement() {}

	public WorldSectionElement(Selection section) {
		this.section = section.copy();
		centerOfRotation = section.getCenter();
	}

	public void mergeOnto(WorldSectionElement other) {
		setVisible(false);
		if (other.isEmpty())
			other.set(section);
		else
			other.add(section);
	}

	public void set(Selection selection) {
		applyNewSelection(selection.copy());
	}

	public void add(Selection toAdd) {
		applyNewSelection(this.section.add(toAdd));
	}

	public void erase(Selection toErase) {
		applyNewSelection(this.section.substract(toErase));
	}

	private void applyNewSelection(Selection selection) {
		this.section = selection;
		queueRedraw();
	}

	public void setCenterOfRotation(Vec3 center) {
		centerOfRotation = center;
	}

	public void stabilizeRotation(Vec3 anchor) {
		stabilizationAnchor = anchor;
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		resetAnimatedTransform();
		resetSelectedBlock();
	}

	public void selectBlock(BlockPos pos) {
		selectedBlock = pos;
	}

	public void resetSelectedBlock() {
		selectedBlock = null;
	}

	public void resetAnimatedTransform() {
		prevAnimatedOffset = Vec3.ZERO;
		animatedOffset = Vec3.ZERO;
		prevAnimatedRotation = Vec3.ZERO;
		animatedRotation = Vec3.ZERO;
	}

	public void queueRedraw() {
		redraw = true;
	}

	public boolean isEmpty() {
		return section == null;
	}

	public void setEmpty() {
		section = null;
	}

	public void setAnimatedRotation(Vec3 eulerAngles, boolean force) {
		this.animatedRotation = eulerAngles;
		if (force)
			prevAnimatedRotation = animatedRotation;
	}

	public Vec3 getAnimatedRotation() {
		return animatedRotation;
	}

	public void setAnimatedOffset(Vec3 offset, boolean force) {
		this.animatedOffset = offset;
		if (force)
			prevAnimatedOffset = animatedOffset;
	}

	public Vec3 getAnimatedOffset() {
		return animatedOffset;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !isEmpty();
	}

	class WorldSectionRayTraceResult {
		Vec3 actualHitVec;
		BlockPos worldPos;
	}

	public Pair<Vec3, BlockPos> rayTrace(PonderWorld world, Vec3 source, Vec3 target) {
		world.setMask(this.section);
		Vec3 transformedTarget = reverseTransformVec(target);
		BlockHitResult rayTraceBlocks = world.clip(new ClipContext(reverseTransformVec(source), transformedTarget,
			ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
		world.clearMask();

		if (rayTraceBlocks == null)
			return null;
		if (rayTraceBlocks.getLocation() == null)
			return null;

		double t = rayTraceBlocks.getLocation()
			.subtract(transformedTarget)
			.lengthSqr()
			/ source.subtract(target)
				.lengthSqr();
		Vec3 actualHit = VecHelper.lerp((float) t, target, source);
		return Pair.of(actualHit, rayTraceBlocks.getBlockPos());
	}

	private Vec3 reverseTransformVec(Vec3 in) {
		float pt = AnimationTickHolder.getPartialTicks();
		in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
			in = in.subtract(centerOfRotation);
			in = VecHelper.rotate(in, -rotX, Axis.X);
			in = VecHelper.rotate(in, -rotZ, Axis.Z);
			in = VecHelper.rotate(in, -rotY, Axis.Y);
			in = in.add(centerOfRotation);
			if (stabilizationAnchor != null) {
				in = in.subtract(stabilizationAnchor);
				in = VecHelper.rotate(in, rotX, Axis.X);
				in = VecHelper.rotate(in, rotZ, Axis.Z);
				in = VecHelper.rotate(in, rotY, Axis.Y);
				in = in.add(stabilizationAnchor);
			}
		}
		return in;
	}

	public void transformMS(PoseStack ms, float pt) {

		Vec3 vec = VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset);
		ms.translate(vec.x, vec.y, vec.z);
		if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);

			ms.translate(centerOfRotation.x, centerOfRotation.y, centerOfRotation.z);
			ms.mulPose(Vector3f.XP.rotationDegrees((float) rotX));
			ms.mulPose(Vector3f.YP.rotationDegrees((float) rotY));
			ms.mulPose(Vector3f.ZP.rotationDegrees((float) rotZ));
			ms.translate(-centerOfRotation.x, -centerOfRotation.y, -centerOfRotation.z);

			if (stabilizationAnchor != null) {

				ms.translate(stabilizationAnchor.x, stabilizationAnchor.y, stabilizationAnchor.z);
				ms.mulPose(Vector3f.XP.rotationDegrees((float) -rotX));
				ms.mulPose(Vector3f.YP.rotationDegrees((float) -rotY));
				ms.mulPose(Vector3f.ZP.rotationDegrees((float) -rotZ));
				ms.translate(-stabilizationAnchor.x, -stabilizationAnchor.y, -stabilizationAnchor.z);
			}
		}
	}

	public void tick(PonderScene scene) {
		prevAnimatedOffset = animatedOffset;
		prevAnimatedRotation = animatedRotation;
		if (!isVisible())
			return;
		loadTEsIfMissing(scene.getWorld());
		renderedTileEntities.removeIf(te -> scene.getWorld()
			.getBlockEntity(te.getBlockPos()) != te);
		tickableTileEntities.removeIf(te -> scene.getWorld()
			.getBlockEntity(te.getFirst()
				.getBlockPos()) != te.getFirst());
		tickableTileEntities.forEach(te -> te.getSecond()
			.accept(scene.getWorld()));
	}

	@Override
	public void whileSkipping(PonderScene scene) {
		if (redraw) {
			renderedTileEntities = null;
			tickableTileEntities = null;
		}
		redraw = false;
	}

	protected void loadTEsIfMissing(PonderWorld world) {
		if (renderedTileEntities != null)
			return;
		tickableTileEntities = new ArrayList<>();
		renderedTileEntities = new ArrayList<>();
		section.forEach(pos -> {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (tileEntity == null)
				return;
			if (!(block instanceof EntityBlock))
				return;
			tileEntity.setBlockState(world.getBlockState(pos));
			BlockEntityTicker<?> ticker = ((EntityBlock) block).getTicker(world, blockState, tileEntity.getType());
			if (ticker != null)
				addTicker(tileEntity, ticker);
			renderedTileEntities.add(tileEntity);
		});
	}

	@SuppressWarnings("unchecked")
	private <T extends BlockEntity> void addTicker(T tileEntity, BlockEntityTicker<?> ticker) {
		tickableTileEntities.add(Pair.of(tileEntity, w -> ((BlockEntityTicker<T>) ticker).tick(w,
			tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity)));
	}

	@Override
	public void renderFirst(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		int light = -1;
		if (fade != 1)
			light = (int) (Mth.lerp(fade, 5, 14));
		if (redraw) {
			renderedTileEntities = null;
			tickableTileEntities = null;
		}

		ms.pushPose();
		transformMS(ms, pt);
		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer, pt);
		world.popLight();

		Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
		PoseStack overlayMS = null;

		for (Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!section.test(pos))
				continue;
			if (overlayMS == null) {
				overlayMS = new PoseStack();
				world.scene.getTransform().apply(overlayMS, pt, true);
				transformMS(overlayMS, pt);
			}

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			VertexConsumer builder = new SheetedDecalTextureGenerator(
				buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(entry.getValue())), overlayMS.last()
					.pose(),
				overlayMS.last()
					.normal());
			CatnipClientServices.CLIENT_HOOKS.renderBlockStateBatched(Minecraft.getInstance().getBlockRenderer(), ms, builder, world.getBlockState(pos), pos, world, true, new Random(), null);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	protected void renderLayer(PonderWorld world, MultiBufferSource buffer, RenderType type, PoseStack ms, float fade, float pt) {
		SuperByteBufferCache bufferCache = SuperByteBufferCache.getInstance();

		int code = hashCode() ^ world.hashCode();
		Pair<Integer, Integer> key = Pair.of(code, RenderType.chunkBufferLayers()
			.indexOf(type));

		if (redraw)
			bufferCache.invalidate(PONDER_WORLD_SECTION, key);

		SuperByteBuffer contraptionBuffer = bufferCache.get(PONDER_WORLD_SECTION, key, () -> buildStructureBuffer(world, type));
		if (contraptionBuffer.isEmpty())
			return;

		transformMS(contraptionBuffer.getTransforms(), pt);
		//transformMS(ms, pt);

		//renderStructureUnbuffered(world, type, ms, buffer.getBuffer(type));

		int light = lightCoordsFromFade(fade);
		contraptionBuffer
			.light(light)
			.renderInto(ms, buffer.getBuffer(type));
	}

	@Override
	protected void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		redraw = false;
		if (selectedBlock == null)
			return;
		BlockState blockState = world.getBlockState(selectedBlock);
		if (blockState.isAir())
			return;
		VoxelShape shape =
			blockState.getShape(world, selectedBlock, CollisionContext.of(Minecraft.getInstance().player));
		if (shape.isEmpty())
			return;

		ms.pushPose();
		transformMS(ms, pt);
		ms.translate(selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());

		AABBOutline aabbOutline = new AABBOutline(shape.bounds());
		aabbOutline.getParams()
			.lineWidth(1 / 64f)
			.colored(0xefefef)
			.disableNormals();
		aabbOutline.render(ms, (SuperRenderTypeBuffer) buffer, pt);

		ms.popPose();
	}

	private void renderTileEntities(PonderWorld world, PoseStack ms, MultiBufferSource buffer, float pt) {
		loadTEsIfMissing(world);

		Iterator<BlockEntity> iterator = renderedTileEntities.iterator();
		while (iterator.hasNext()) {
			BlockEntity tile = iterator.next();
			BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
			if (renderer == null) {
				iterator.remove();
				continue;
			}

			BlockPos pos = tile.getBlockPos();
			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			try {
				renderer.render(tile, pt, ms, buffer, LevelRenderer.getLightColor(world, pos), OverlayTexture.NO_OVERLAY);

			} catch (Exception e) {
				iterator.remove();
				String message = "BlockEntity " + CatnipServices.REGISTRIES.getKeyOrThrow(tile.getType())
						.toString() + " could not be rendered virtually.";
				Ponder.LOGGER.error(message, e);
			}

			ms.popPose();
		}
		//TileEntityRenderHelper.renderTileEntities(world, renderedTileEntities, ms, buffer, pt);
	}

	/*private void renderStructureUnbuffered(PonderWorld world, RenderType layer, PoseStack ms, VertexConsumer consumer) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = ms;
		Random random = objects.random;
		//BufferBuilder bufferBuilder = objects.unshadedBuilder;


		//bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		world.setMask(this.section);
		ModelBlockRenderer.enableCaching();

		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			FluidState fluidState = world.getFluidState(pos);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());


			if (state.getRenderShape() == RenderShape.MODEL && CatnipClientServices.CLIENT_HOOKS.chunkRenderTypeMatches(state, layer)) {
				BlockEntity tile = world.getBlockEntity(pos);
				CatnipClientServices.CLIENT_HOOKS.renderBlockStateBatched(dispatcher, poseStack, consumer, state, pos, world, true, random, tile);
			}

			if (!fluidState.isEmpty() && CatnipClientServices.CLIENT_HOOKS.fluidRenderTypeMatches(fluidState, layer)) {
				dispatcher.renderLiquid(pos, world, consumer, state, fluidState);
			}

			poseStack.popPose();
		});

		ModelBlockRenderer.clearCache();
		world.clearMask();
		//bufferBuilder.end();
	}*/

	private SuperByteBuffer buildStructureBuffer(PonderWorld world, RenderType layer) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		Random random = objects.random;
		BufferBuilder builder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

		world.setMask(this.section);
		//ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			FluidState fluidState = world.getFluidState(pos);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

			if (state.getRenderShape() == RenderShape.MODEL && CatnipClientServices.CLIENT_HOOKS.chunkRenderTypeMatches(state, layer)) {
				BlockEntity tile = world.getBlockEntity(pos);
				CatnipClientServices.CLIENT_HOOKS.renderBlockStateBatched(dispatcher, poseStack, builder, state, pos, world, true, random, tile);
			}

			if (!fluidState.isEmpty() && CatnipClientServices.CLIENT_HOOKS.fluidRenderTypeMatches(fluidState, layer))
				dispatcher.renderLiquid(pos, world, builder, state, fluidState);

			poseStack.popPose();
		});
		ModelBlockRenderer.clearCache();
		//ForgeHooksClient.setRenderType(null);
		world.clearMask();

		builder.end();

		return SuperBufferFactory.getInstance().create(builder);
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final Random random = new Random();
		//public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
