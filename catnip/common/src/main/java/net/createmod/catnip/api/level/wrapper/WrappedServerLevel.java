package net.createmod.catnip.api.level.wrapper;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.createmod.catnip.impl.mixin.BiomeManagerAccessor;
import net.createmod.catnip.impl.mixin.EntityAccessor;
import net.createmod.catnip.impl.mixin.MinecraftServerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.TickPriority;

public class WrappedServerLevel extends ServerLevel {
	protected ServerLevel level;

	public WrappedServerLevel(ServerLevel level) {
		LevelStorageAccess storage = ((MinecraftServerAccessor) level.getServer()).catnip$getStorageSource();
		ServerLevelData data = (ServerLevelData) level.getLevelData();
		LevelStem stem = new LevelStem(level.dimensionTypeRegistration(), level.getChunkSource().getGenerator());
		long seed = ((BiomeManagerAccessor) level.getBiomeManager()).catnip$getBiomeZoomSeed();

		super(level.getServer(), Util.backgroundExecutor(), storage, data, level.dimension(), stem, level.isDebug(), seed, Collections.emptyList(), false);
		this.level = level;
	}

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		level.sendBlockUpdated(pos, oldState, newState, flags);
	}

	@Override
	public LevelTicks<Block> getBlockTicks() {
		return super.getBlockTicks();
	}

	@Override
	public LevelTicks<Fluid> getFluidTicks() {
		return super.getFluidTicks();
	}

	@Override
	public void scheduleTick(BlockPos pos, Block block, int delay) {
	}

	@Override
	public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
	}

	@Override
	public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {
	}

	@Override
	public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
	}

	@Override
	public void levelEvent(@Nullable Entity entity, int type, BlockPos pos, int data) {
	}

	@Override
	public List<ServerPlayer> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(@Nullable Entity except, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
	}

	@Override
	public void playSound(@Nullable Entity except, Entity sourceEntity, SoundEvent sound, SoundSource source, float volume, float pitch) {
	}

	@Override
	public @Nullable Entity getEntity(int id) {
		return null;
	}

	@Override
	public @Nullable MapItemSavedData getMapData(MapId mapId) {
		return null;
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		((EntityAccessor) entityIn).catnip$callSetLevel(level);
		return level.addFreshEntity(entityIn);
	}

	@Override
	public void setMapData(MapId mapId, MapItemSavedData mapData) {
	}

	@Override
	public MapId getFreeMapId() {
		return new MapId(0);
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public RecipeManager recipeAccess() {
		return level.recipeAccess();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
		return level.getUncachedNoiseBiome(quartX, quartY, quartZ);
	}
}
