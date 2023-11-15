package net.createmod.ponder.foundation.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData;
import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.ShadeSpearatingSuperByteBuffer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperByteBufferCache.Compartment;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.outliner.AABBOutline;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import net.minecraft.util.RandomSource;
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

public class WorldSectionElementImpl extends AnimatedSceneElementBase implements WorldSectionElement {

	public static final Compartment<Pair<Integer, Integer>> PONDER_WORLD_SECTION = new Compartment<>();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	@Nullable List<BlockEntity> renderedBlockEntities;
	@Nullable List<Pair<BlockEntity, Consumer<Level>>> tickableBlockEntities;
	@Nullable Selection section;
	boolean redraw;

	Vec3 prevAnimatedOffset = Vec3.ZERO;
	Vec3 animatedOffset = Vec3.ZERO;
	Vec3 prevAnimatedRotation = Vec3.ZERO;
	Vec3 animatedRotation = Vec3.ZERO;
	Vec3 centerOfRotation = Vec3.ZERO;
	@Nullable Vec3 stabilizationAnchor = null;

	@Nullable BlockPos selectedBlock;

	public WorldSectionElementImpl() {}

	public WorldSectionElementImpl(Selection section) {
		this.section = section.copy();
		centerOfRotation = section.getCenter();
	}

	@Override
	public void mergeOnto(WorldSectionElement other) {
		setVisible(false);
		if (other.isEmpty())
			other.set(section);
		else
			other.add(section);
	}

	@Override
	public void set(Selection selection) {
		applyNewSelection(selection.copy());
	}

	@Override
	public void add(Selection toAdd) {
		applyNewSelection(this.section.add(toAdd));
	}

	@Override
	public void erase(Selection toErase) {
		applyNewSelection(this.section.substract(toErase));
	}

	private void applyNewSelection(Selection selection) {
		this.section = selection;
		queueRedraw();
	}

	@Override
	public void setCenterOfRotation(Vec3 center) {
		centerOfRotation = center;
	}

	@Override
	public void stabilizeRotation(Vec3 anchor) {
		stabilizationAnchor = anchor;
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		resetAnimatedTransform();
		resetSelectedBlock();
	}

	@Override
	public void selectBlock(BlockPos pos) {
		selectedBlock = pos;
	}

	@Override
	public void resetSelectedBlock() {
		selectedBlock = null;
	}

	public void resetAnimatedTransform() {
		prevAnimatedOffset = Vec3.ZERO;
		animatedOffset = Vec3.ZERO;
		prevAnimatedRotation = Vec3.ZERO;
		animatedRotation = Vec3.ZERO;
	}

	@Override
	public void queueRedraw() {
		redraw = true;
	}

	@Override
	public boolean isEmpty() {
		return section == null;
	}

	@Override
	public void setEmpty() {
		section = null;
	}

	@Override
	public void setAnimatedRotation(Vec3 eulerAngles, boolean force) {
		this.animatedRotation = eulerAngles;
		if (force)
			prevAnimatedRotation = animatedRotation;
	}

	@Override
	public Vec3 getAnimatedRotation() {
		return animatedRotation;
	}

	@Override
	public void setAnimatedOffset(Vec3 offset, boolean force) {
		this.animatedOffset = offset;
		if (force)
			prevAnimatedOffset = animatedOffset;
	}

	@Override
	public Vec3 getAnimatedOffset() {
		return animatedOffset;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !isEmpty();
	}

	@Override
	public Pair<Vec3, BlockHitResult> rayTrace(PonderLevel world, Vec3 source, Vec3 target) {
		world.setMask(this.section);
		Vec3 transformedTarget = reverseTransformVec(target);
		BlockHitResult rayTraceBlocks = world.clip(new ClipContext(reverseTransformVec(source), transformedTarget,
			ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
		world.clearMask();

		double t = rayTraceBlocks.getLocation()
			.subtract(transformedTarget)
			.lengthSqr()
			/ source.subtract(target)
				.lengthSqr();
		Vec3 actualHit = VecHelper.lerp((float) t, target, source);
		return Pair.of(actualHit, rayTraceBlocks);
	}

	private Vec3 reverseTransformVec(Vec3 in) {
		float pt = AnimationTickHolder.getPartialTicks();
		in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
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
            double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);

			ms.translate(centerOfRotation.x, centerOfRotation.y, centerOfRotation.z);
			ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) rotX));
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) rotY));
			ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) rotZ));
			ms.translate(-centerOfRotation.x, -centerOfRotation.y, -centerOfRotation.z);

			if (stabilizationAnchor != null) {

				ms.translate(stabilizationAnchor.x, stabilizationAnchor.y, stabilizationAnchor.z);
				ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) -rotX));
				ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) -rotY));
				ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) -rotZ));
				ms.translate(-stabilizationAnchor.x, -stabilizationAnchor.y, -stabilizationAnchor.z);
			}
		}
	}

	@Override
	public void tick(PonderScene scene) {
		prevAnimatedOffset = animatedOffset;
		prevAnimatedRotation = animatedRotation;
		if (!isVisible())
			return;
		loadBEsIfMissing(scene.getWorld());
		renderedBlockEntities.removeIf(be -> scene.getWorld()
			.getBlockEntity(be.getBlockPos()) != be);
		tickableBlockEntities.removeIf(be -> scene.getWorld()
			.getBlockEntity(be.getFirst()
				.getBlockPos()) != be.getFirst());
		tickableBlockEntities.forEach(be -> be.getSecond()
			.accept(scene.getWorld()));
	}

	@Override
	public void whileSkipping(PonderScene scene) {
		if (redraw) {
			renderedBlockEntities = null;
			tickableBlockEntities = null;
		}
		redraw = false;
	}

	protected void loadBEsIfMissing(PonderLevel world) {
		if (renderedBlockEntities != null)
			return;
		tickableBlockEntities = new ArrayList<>();
		renderedBlockEntities = new ArrayList<>();
		section.forEach(pos -> {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (blockEntity == null)
				return;
			if (!(block instanceof EntityBlock))
				return;
			blockEntity.setBlockState(world.getBlockState(pos));
			BlockEntityTicker<?> ticker = ((EntityBlock) block).getTicker(world, blockState, blockEntity.getType());
			if (ticker != null)
				addTicker(blockEntity, ticker);
			renderedBlockEntities.add(blockEntity);
		});
	}

	@SuppressWarnings("unchecked")
	private <T extends BlockEntity> void addTicker(T blockEntity, BlockEntityTicker<?> ticker) {
		tickableBlockEntities.add(Pair.of(blockEntity, w -> ((BlockEntityTicker<T>) ticker).tick(w,
			blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity)));
	}

	@Override
	public void renderFirst(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {
		PoseStack poseStack = graphics.pose();
		int light = -1;
		if (fade != 1)
			light = (int) (Mth.lerp(fade, 5, 14));
		if (redraw) {
			renderedBlockEntities = null;
			tickableBlockEntities = null;
		}

		poseStack.pushPose();
		transformMS(poseStack, pt);
		world.pushFakeLight(light);
		renderBlockEntities(world, poseStack, buffer, pt);
		world.popLight();

		Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
		PoseStack overlayMS = null;

		for (Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!section.test(pos))
				continue;

			if (overlayMS == null) {
				overlayMS = new PoseStack();
				overlayMS.last().pose().set(poseStack.last().pose());
				overlayMS.last().normal().set(poseStack.last().normal());

				float scaleFactor = world.scene.getScaleFactor();
				float f = (float) Math.pow(30 * scaleFactor, -1.2);
				overlayMS.scale(f, f, f);
			}

			VertexConsumer builder = new SheetedDecalTextureGenerator(
					buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(entry.getValue())), overlayMS.last().pose(),
					overlayMS.last().normal(),
					1);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(world.getBlockState(pos), pos, world, poseStack, builder);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	@Override
	protected void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, GuiGraphics graphics, float fade, float pt) {
		PoseStack poseStack = graphics.pose();
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
			.renderInto(poseStack, buffer.getBuffer(type));
	}

	@Override
	protected void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {
		PoseStack poseStack = graphics.pose();
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

		poseStack.pushPose();
		transformMS(poseStack, pt);
		poseStack.translate(selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());

		AABBOutline aabbOutline = new AABBOutline(shape.bounds());
		aabbOutline.getParams()
			.lineWidth(1 / 64f)
			.colored(0xefefef)
			.disableLineNormals();
		aabbOutline.render(poseStack, (SuperRenderTypeBuffer) buffer, Vec3.ZERO, pt);

		poseStack.popPose();
	}

	private void renderBlockEntities(PonderLevel world, PoseStack ms, MultiBufferSource buffer, float pt) {
		loadBEsIfMissing(world);

		Iterator<BlockEntity> iterator = renderedBlockEntities.iterator();
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
	}

	private SuperByteBuffer buildStructureBuffer(PonderLevel world, RenderType layer) {
		BlockRenderDispatcher dispatcher = CatnipClientServices.CLIENT_HOOKS.getBlockRenderDispatcher();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		RandomSource random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder shadedBuilder = objects.shadedBuilder;
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		shadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(shadedBuilder, unshadedBuilder);

		world.setMask(this.section);
		ModelBlockRenderer.enableCaching();
		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			FluidState fluidState = world.getFluidState(pos);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

			if (state.getRenderShape() == RenderShape.MODEL) {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				state.getSeed(pos);

				if (CatnipClientServices.CLIENT_HOOKS.doesBlockModelContainRenderType(layer, state, random, blockEntity))
					CatnipClientServices.CLIENT_HOOKS.renderBlockStateBatched(dispatcher, poseStack, shadeSeparatingWrapper, state, pos, world, true, random, layer, blockEntity);

			}

			if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == layer)
				dispatcher.renderLiquid(pos, world, shadedBuilder, state, fluidState);

			poseStack.popPose();
		});
		ModelBlockRenderer.clearCache();
		world.clearMask();

		shadeSeparatingWrapper.clear();
		ShadeSeparatedBufferedData bufferedData = ModelUtil.endAndCombine(shadedBuilder, unshadedBuilder);

		SuperByteBuffer sbb = new ShadeSpearatingSuperByteBuffer(bufferedData);
		bufferedData.release();
		return sbb;
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder shadedBuilder = new BufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
