package net.createmod.catnip.levelWrappers;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.item.crafting.RecipeAccess;

import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.LevelData.RespawnData;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class SchematicChunkSource extends ChunkSource {
	private final Level fallbackWorld;

	public SchematicChunkSource(Level world) {
		fallbackWorld = world;
	}

	@Nullable
	@Override
	public LightChunk getChunkForLighting(int x, int z) {
		return getChunk(x, z);
	}

	@Override
	public Level getLevel() {
		return fallbackWorld;
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_) {
		return getChunk(x, z);
	}

	public ChunkAccess getChunk(int x, int z) {
		return new EmptierChunk(fallbackWorld);
	}

	@Override
	public String gatherStats() {
		return "WrappedChunkProvider";
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return fallbackWorld.getLightEngine();
	}

	@Override
	public void tick(BooleanSupplier p_202162_, boolean p_202163_) {
	}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}

	public static class EmptierChunk extends LevelChunk {

		private static final class DummyLevel extends Level {
			private DummyLevel(WritableLevelData pLevelData, ResourceKey<Level> pDimension,
							   RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration,
							   boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed,
							   int pMaxChainedNeighborUpdates) {
				super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pIsClientSide, pIsDebug,
					pBiomeZoomSeed, pMaxChainedNeighborUpdates);
				access = pRegistryAccess;
			}

			private final RegistryAccess access;

			private DummyLevel(Level level) {
				this(null, null, level.registryAccess(), level.dimensionTypeRegistration(), false, false, 0, 0);
			}

			@Override
			public ChunkSource getChunkSource() {
				return null;
			}

			@Override
			public void levelEvent(@Nullable Entity except, int type, BlockPos pos, int data) {
			}

			@Override
			public void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, Vec3 pos) {
			}

			@Override
			public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
			}

			@Override
			public RegistryAccess registryAccess() {
				return access;
			}

			@Override
			public EnvironmentAttributeSystem environmentAttributes() {
				return null;
			}

			@Override
			public PotionBrewing potionBrewing() {
				return null;
			}

			@Override
			public FuelValues fuelValues() {
				return null;
			}

			@Override
			public List<? extends Player> players() {
				return null;
			}

			@Override
			public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
				return null;
			}

			@Override
			public int getSeaLevel() {
				return 0;
			}

			@Override
			public float getShade(Direction pDirection, boolean pShade) {
				return 0;
			}

			@Override
			public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
			}

			@Override
			public void playSound(@Nullable Entity except, double x, double y, double z, SoundEvent sound,
								  SoundSource source, float volume, float pitch) {
			}

			@Override
			public void playSound(@Nullable Entity except, Entity sourceEntity, SoundEvent sound, SoundSource category,
								  float volume, float pitch) {
			}

			@Override
			public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
								@Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z,
								float radius, boolean fire, ExplosionInteraction explosionInteraction,
								ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
								WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
			}

			@Override
			public void playSeededSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound,
										SoundSource source, float volume, float pitch, long seed) {
			}

			@Override
			public void playSeededSound(@Nullable Entity except, double x, double y, double z, SoundEvent sound,
										SoundSource source, float volume, float pitch, long seed) {
			}

			@Override
			public void playSeededSound(@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound,
										SoundSource source, float volume, float pitch, long seed) {
			}

			@Override
			public String gatherChunkSourceStats() {
				return null;
			}

			@Override
			public void setRespawnData(RespawnData respawnData) {}

			@Override
			public RespawnData getRespawnData() {
				return null;
			}

			@Override
			public Entity getEntity(int pId) {
				return null;
			}

			@Override
			public Collection<EnderDragonPart> dragonParts() {
				return null;
			}

			@Nullable
			@Override
			public MapItemSavedData getMapData(MapId mapId) {
				return null;
			}

			@Override
			public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
			}

			@Override
			public Scoreboard getScoreboard() {
				return null;
			}

			@Override
			public RecipeAccess recipeAccess() {
				return null;
			}

			@Override
			public WorldBorder getWorldBorder() {
				return null;
			}

			@Override
			protected LevelEntityGetter<Entity> getEntities() {
				return null;
			}

			@Override
			public LevelTickAccess<Block> getBlockTicks() {
				return BlackholeTickAccess.emptyLevelList();
			}

			@Override
			public LevelTickAccess<Fluid> getFluidTicks() {
				return BlackholeTickAccess.emptyLevelList();
			}

			@Override
			public FeatureFlagSet enabledFeatures() {
				return FeatureFlagSet.of();
			}

			@Override
			public TickRateManager tickRateManager() {
				return null;
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

		public EmptierChunk(Level level) {
			super(new DummyLevel(level), ChunkPos.ZERO);
		}

		public BlockState getBlockState(BlockPos p_180495_1_) {
			return Blocks.VOID_AIR.defaultBlockState();
		}

		public FluidState getFluidState(BlockPos pos) {
			return Fluids.EMPTY.defaultFluidState();
		}

		public int getLightEmission(BlockPos pos) {
			return 0;
		}

		@Nullable
		public BlockEntity getBlockEntity(BlockPos pos, EntityCreationType type) {
			return null;
		}

		public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
		}

		public void setBlockEntity(BlockEntity blockEntity) {
		}

		public void removeBlockEntity(BlockPos pos) {
		}

		public void markUnsaved() {
		}

		public boolean isEmpty() {
			return true;
		}

		public boolean isYSpaceEmpty(int yStartInclusive, int yEndInclusive) {
			return true;
		}

		public FullChunkStatus getFullStatus() {
			return FullChunkStatus.FULL;
		}
	}
}
