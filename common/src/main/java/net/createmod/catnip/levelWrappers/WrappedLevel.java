package net.createmod.catnip.levelWrappers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.createmod.ponder.mixin.accessor.EntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

public class WrappedLevel extends Level {
	protected Level level;
	protected ChunkSource chunkSource;

	protected LevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();

	public WrappedLevel(Level level) {
		super((WritableLevelData) level.getLevelData(), level.dimension(), level.registryAccess(),
			level.dimensionTypeRegistration(), level.isClientSide(), level.isDebug(), 0, 0);
		this.level = level;
	}

	public void setChunkSource(ChunkSource source) {
		this.chunkSource = source;
	}

	public Level getLevel() {
		return level;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return level.getLightEngine();
	}

	@Override
	public BlockState getBlockState(@Nullable BlockPos pos) {
		return level.getBlockState(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
		return level.isStateAtPosition(pos, predicate);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return level.getBlockEntity(pos);
	}

	@Override
	public void setRespawnData(RespawnData respawnData) {
		level.setRespawnData(respawnData);
	}

	@Override
	public RespawnData getRespawnData() {
		return level.getRespawnData();
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		return level.setBlock(pos, newState, flags);
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
	public LevelTickAccess<Block> getBlockTicks() {
		return level.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return level.getFluidTicks();
	}

	@Override
	public ChunkSource getChunkSource() {
		return chunkSource != null ? chunkSource : level.getChunkSource();
	}

	@Override
	public void levelEvent(@Nullable Entity entity, int type, BlockPos pos, int data) {
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSeededSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound,
								SoundSource source, float volume, float pitch, long seed) {
	}

	@Override
	public void playSeededSound(@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source,
								float volume, float pitch, long seed) {
	}

	@Override
	public void playSound(@Nullable Entity sourceEntity, double x, double y, double z, SoundEvent sound,
						  SoundSource source, float volume, float pitch) {
	}

	@Override
	public void playSound(@Nullable Entity except, Entity entity, SoundEvent sound,
						  SoundSource source, float volume, float pitch) {
	}

	@Override
	public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
						@Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r,
						boolean fire, Level.ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles,
						ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles,
						Holder<SoundEvent> explosionSound) {
	}

	@Override
	public @Nullable Entity getEntity(int id) {
		return null;
	}

	@Override
	public Collection<EnderDragonPart> dragonParts() {
		return List.of();
	}

	@Override
	public TickRateManager tickRateManager() {
		return level.tickRateManager();
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(MapId mapId) {
		return null;
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		((EntityAccessor) entityIn).catnip$callSetLevel(level);
		return level.addFreshEntity(entityIn);
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public Scoreboard getScoreboard() {
		return level.getScoreboard();
	}

	@Override
	public RecipeAccess recipeAccess() {
		return level.recipeAccess();
	}

	@Override
	public FuelValues fuelValues() {
		return level.fuelValues();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
		return level.getUncachedNoiseBiome(quartX, quartY, quartZ);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public RegistryAccess registryAccess() {
		return level.registryAccess();
	}

	@Override
	public EnvironmentAttributeSystem environmentAttributes() {
		return level.environmentAttributes();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return level.getWorldBorder();
	}

	@Override
	public PotionBrewing potionBrewing() {
		return level.potionBrewing();
	}

	@Override
	public float getShade(Direction face, boolean hasShade) {
		return level.getShade(face, hasShade);
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
	}

	@Override
	public void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, Vec3 pos) {
	}

	@Override
	public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
	}

	@Override
	public String gatherChunkSourceStats() {
		return level.gatherChunkSourceStats();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	// Intentionally copied from LevelHeightAccessor. Workaround for issues caused
	// when other mods (such as Lithium)
	// override the vanilla implementations in ways which cause WrappedWorlds to
	// return incorrect, default height info.
	// WrappedWorld subclasses should implement their own getMinBuildHeight and
	// getHeight overrides where they deviate
	// from the defaults for their dimension.

	@Override
	public int getMaxY() {
		return this.getMinY() + this.getHeight();
	}

	@Override
	public int getSectionsCount() {
		return this.getMaxSectionY() - this.getMinSectionY();
	}

	@Override
	public int getMinSectionY() {
		return SectionPos.blockToSectionCoord(this.getMinY());
	}

	@Override
	public int getMaxSectionY() {
		return SectionPos.blockToSectionCoord(this.getMaxY() - 1) + 1;
	}

	@Override
	public boolean isOutsideBuildHeight(BlockPos pos) {
		return this.isOutsideBuildHeight(pos.getY());
	}

	@Override
	public boolean isOutsideBuildHeight(int y) {
		return y < this.getMinY() || y >= this.getMaxY();
	}

	@Override
	public int getSectionIndex(int y) {
		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y));
	}

	@Override
	public int getSectionIndexFromSectionY(int sectionY) {
		return sectionY - this.getMinSectionY();
	}

	@Override
	public int getSectionYFromSectionIndex(int sectionIndex) {
		return sectionIndex + this.getMinSectionY();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return level.enabledFeatures();
	}

	// Neo's patched methods
	public void setDayTimeFraction(float f) {
	}

	public float getDayTimeFraction() {
		return 0;
	}

	public void setDayTimePerTick(float f) {
	}

	public float getDayTimePerTick() {
		return 0;
	}
}
