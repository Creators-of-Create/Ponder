package net.createmod.ponder.api.client.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.level.wrapper.WrappedClientLevel;
import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.VirtualBlockEntity;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.api.client.scene.Selection;
import net.createmod.ponder.impl.client.scene.PonderWorldParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PonderLevel extends SchematicLevel implements BlockAndTintGetter {

	@Nullable
	public PonderScene scene;

	protected Map<BlockPos, BlockState> originalBlocks;
	protected Map<BlockPos, CompoundTag> originalBlockEntities;
	protected Map<BlockPos, Integer> blockBreakingProgressions;
	protected List<Entity> originalEntities;
	private final Supplier<ClientLevel> asClientWorld = Suppliers.memoize(() -> WrappedClientLevel.of(this));

	protected PonderWorldParticles particles;

	int overrideLight;
	@Nullable
	Selection mask;
	boolean currentlyTickingEntities;
	CardinalLighting overrideLighting;

	public PonderLevel(BlockPos anchor, Level original) {
		super(anchor, original);
		originalBlocks = new HashMap<>();
		originalBlockEntities = new HashMap<>();
		blockBreakingProgressions = new HashMap<>();
		originalEntities = new ArrayList<>();
		particles = new PonderWorldParticles(this);
		renderMode = true;
	}

	public void createBackup() {
		originalBlocks.clear();
		originalBlockEntities.clear();
		originalBlocks.putAll(blocks);
		blockEntities.forEach(
			(k, v) -> originalBlockEntities.put(k, v.saveWithFullMetadata(registryAccess())));
		entities.forEach(e -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(e.problemPath(), Ponder.LOGGER)) {
				TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess());
				e.save(output);
				ValueInput input = TagValueInput.create(reporter, registryAccess(), output.buildResult());
				EntityType.create(input, this, new EntitySpawnRequest(EntitySpawnReason.LOAD, false)).ifPresent(originalEntities::add);
			}
		});
	}

	public void restore() {
		entities.clear();
		blocks.clear();
		blockEntities.clear();
		blockBreakingProgressions.clear();
		renderedBlockEntities.clear();
		blocks.putAll(originalBlocks);
		originalBlockEntities.forEach((k, v) -> {
			BlockEntity blockEntity = BlockEntity.loadStatic(k, originalBlocks.get(k), v, registryAccess());
			onBEAdded(blockEntity, blockEntity.getBlockPos());
			blockEntities.put(k, blockEntity);
			renderedBlockEntities.add(blockEntity);
		});
		originalEntities.forEach(e -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(e.problemPath(), Ponder.LOGGER)) {
				TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess());
				e.save(output);
				ValueInput input = TagValueInput.create(reporter, registryAccess(), output.buildResult());
				EntityType.create(input, this, new EntitySpawnRequest(EntitySpawnReason.LOAD, false)).ifPresent(entities::add);
			}
		});
		particles.clearEffects();

		PonderIndex.forEachPlugin(plugin -> plugin.onPonderLevelRestore(this));
	}

	public void restoreBlocks(Selection selection) {
		selection.forEach(p -> {
			if (originalBlocks.containsKey(p))
				blocks.put(p, originalBlocks.get(p));
			if (originalBlockEntities.containsKey(p)) {
				BlockEntity blockEntity = BlockEntity.loadStatic(p, originalBlocks.get(p), originalBlockEntities.get(p), registryAccess());
				if (blockEntity != null) {
					onBEAdded(blockEntity, blockEntity.getBlockPos());
					blockEntities.put(p, blockEntity);
				}
			}
		});
		redraw();
	}

	private void redraw() {
		if (scene != null)
			scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
	}

	public void pushFakeLight(int light) {
		this.overrideLight = light;
	}

	public void popLight() {
		this.overrideLight = -1;
	}

	@Override
	public int getBrightness(LightLayer p_226658_1_, BlockPos p_226658_2_) {
		return overrideLight == -1 ? 15 : overrideLight;
	}

	public void setMask(@Nullable Selection mask) {
		this.mask = mask;
	}

	public void clearMask() {
		this.mask = null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		if (mask != null && !mask.test(globalPos.subtract(anchor)))
			return Blocks.AIR.defaultBlockState();
		if (currentlyTickingEntities && globalPos.getY() < 0)
			return Blocks.AIR.defaultBlockState();
		return super.getBlockState(globalPos);
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver color) {
		if (this.level instanceof BlockAndTintGetter tintGetter) {
			return tintGetter.getBlockTint(pos, color);
		}

		return 0xFFFFFFFF;
	}

	public void pushCardinalLighting(CardinalLighting lighting) {
		this.overrideLighting = lighting;
	}

	public void popCardinalLighting() {
		this.overrideLighting = null;
	}

	@Override
	public CardinalLighting cardinalLighting() {
		if (this.overrideLighting != null) {
			return this.overrideLighting;
		}

		if (this.level instanceof BlockAndTintGetter tintGetter) {
			return tintGetter.cardinalLighting();
		}

		return CardinalLighting.DEFAULT;
	}

	@Override // For particle collision
	public BlockGetter getChunkForCollisions(int p_225522_1_, int p_225522_2_) {
		return this;
	}

	public void renderEntities(PoseStack poseStack, SubmitNodeCollector queue, Camera camera,
	                           CameraRenderState cameraRenderState, float pt) {
		Vec3 vec3 = camera.position();
		double camX = vec3.x();
		double camY = vec3.y();
		double camZ = vec3.z();

		for (Entity entity : entities) {
			if (entity.tickCount == 0) {
				entity.xOld = entity.getX();
				entity.yOld = entity.getY();
				entity.zOld = entity.getZ();
			}

			EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			EntityRenderState state = dispatcher.extractEntity(entity, pt);
			dispatcher.submit(state, cameraRenderState, state.x - camX, state.y - camY, state.z - camZ, poseStack, queue);
		}
	}

	public void renderParticles(PoseStack ms, SubmitNodeCollector queue, Camera camera,
								CameraRenderState cameraRenderState, float pt) {
		particles.renderParticles(ms, queue, camera, cameraRenderState, pt);
	}

	public void tick() {
		currentlyTickingEntities = true;

		particles.tick();

		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
			Entity entity = iterator.next();

			entity.tickCount++;
			entity.xOld = entity.getX();
			entity.yOld = entity.getY();
			entity.zOld = entity.getZ();
			entity.tick();

			if (entity.getY() <= -.5f)
				entity.discard();

			if (!entity.isAlive())
				iterator.remove();
		}

		currentlyTickingEntities = false;
	}

	@Override
	public void addParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(makeParticle(data, x, y, z, mx, my, mz));
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(data, x, y, z, mx, my, mz);
	}

	@Nullable
	private <T extends ParticleOptions> Particle makeParticle(T data, double x, double y, double z, double mx, double my,
															  double mz) {
		return ModClientHooksHelper.INSTANCE.createParticleFromData(data, asClientWorld.get(), x, y, z, mx, my, mz);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState arg1, int arg2) {
		return super.setBlock(pos, arg1, arg2);
	}

	public void addParticle(@Nullable Particle p) {
		if (p != null)
			particles.addParticle(p);
	}

	protected void onBEAdded(BlockEntity blockEntity, BlockPos pos) {
		super.onBEadded(blockEntity, pos);
		if (!(blockEntity instanceof VirtualBlockEntity virtualBlockEntity))
			return;
		virtualBlockEntity.markVirtual();
	}

	public void setBlockBreakingProgress(BlockPos pos, int damage) {
		if (damage == 0)
			blockBreakingProgressions.remove(pos);
		else
			blockBreakingProgressions.put(pos, damage - 1);
	}

	public Map<BlockPos, Integer> getBlockBreakingProgressions() {
		return blockBreakingProgressions;
	}

	public void addBlockDestroyEffects(BlockPos pos, BlockState state) {
		VoxelShape voxelshape = state.getShape(this, pos);
		if (voxelshape.isEmpty())
			return;

		AABB bb = voxelshape.bounds();
		double d1 = Math.min(1.0D, bb.maxX - bb.minX);
		double d2 = Math.min(1.0D, bb.maxY - bb.minY);
		double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
		int i = Math.max(2, Mth.ceil(d1 / 0.25D));
		int j = Math.max(2, Mth.ceil(d2 / 0.25D));
		int k = Math.max(2, Mth.ceil(d3 / 0.25D));

		for (int l = 0; l < i; ++l) {
			for (int i1 = 0; i1 < j; ++i1) {
				for (int j1 = 0; j1 < k; ++j1) {
					double d4 = (l + 0.5D) / i;
					double d5 = (i1 + 0.5D) / j;
					double d6 = (j1 + 0.5D) / k;
					double d7 = d4 * d1 + bb.minX;
					double d8 = d5 * d2 + bb.minY;
					double d9 = d6 * d3 + bb.minZ;
					addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + d7, pos.getY() + d8,
						pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D);
				}
			}
		}
	}

	@Override
	protected BlockState processBlockStateForPrinting(BlockState state) {
		return state;
	}

	@Override
	public boolean hasChunkAt(BlockPos pos) {
		return true; // fix particle lighting
	}

	@Override
	public boolean hasChunk(int x, int y) {
		return true; // fix particle lighting
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true; // fix particle lighting
	}

	@Override
	public boolean hasNearbyAlivePlayer(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
		return true; // always enable spawner animations
	}
}
