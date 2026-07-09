package net.createmod.catnip.api.client.level.wrapper;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.createmod.catnip.impl.mixin.BiomeManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class WrappedClientLevel extends ClientLevel {
	private static final Minecraft mc = Minecraft.getInstance();
	protected Level level;

	private WrappedClientLevel(Level level) {
		// shouldn't be null, given level should have the same instance
		ClientPacketListener connection = Objects.requireNonNull(mc.getConnection(), "connection");
		int chunkRadius = mc.options.getEffectiveRenderDistance();
		long seed = ((BiomeManagerAccessor) level.getBiomeManager()).catnip$getBiomeZoomSeed();
		int simDistance = Objects.requireNonNull(mc.level).getServerSimulationDistance();

		super(
			connection, mc.level.getLevelData(), level.dimension(), level.dimensionTypeRegistration(),
			chunkRadius, simDistance, mc.levelExtractor, level.isDebug(), seed, level.getSeaLevel()
		);

		this.level = level;
	}

	public static WrappedClientLevel of(Level level) {
		return new WrappedClientLevel(level);
	}

	@Override
	public boolean hasChunkAt(BlockPos pos) {
		return level.hasChunkAt(pos);
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return level.isLoaded(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return level.getBlockState(pos);
	}

	@Override
	public @Nullable BlockGetter getChunkForCollisions(int x, int z) {
		return level.getChunkForCollisions(x, z);
	}

	// FIXME: blockstate#getCollisionShape with WrappedClientWorld gives unreliable
	// data (maybe)

	@Override
	public int getBrightness(LightLayer type, BlockPos pos) {
		return level.getBrightness(type, pos);
	}

	@Override
	public int getLightEmission(BlockPos pos) {
		return level.getLightEmission(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return level.getFluidState(pos);
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver resolver) {
		if (this.level instanceof BlockAndTintGetter tintGetter) {
			return tintGetter.getBlockTint(pos, resolver);
		}

		return 0xFFFFFFFF;
	}

	// FIXME: Emissive Lighting might not light stuff properly

	@Override
	public void addParticle(ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd) {
		level.addParticle(particle, x, y, z, xd, yd, zd);
	}

	@Override
	public void addParticle(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShow,
							double x, double y, double z, double xd, double yd, double zd) {
		level.addParticle(particle, overrideLimiter, alwaysShow, x, y, z, xd, yd, zd);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions options, double x, double y, double z, double xd, double yd, double zd) {
		level.addAlwaysVisibleParticle(options, x, y, z, xd, yd, zd);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions options, boolean overrideLimiter,
										 double x, double y, double z, double xd, double yd, double zd) {
		level.addAlwaysVisibleParticle(options, overrideLimiter, x, y, z, xd, yd, zd);
	}

	@Override
	public void playLocalSound(double x, double y, double z, SoundEvent sound,
							   SoundSource source, float volume, float pitch, boolean distanceDelay) {
		level.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
	}

	@Override
	public void playSound(@Nullable Entity except, double x, double y, double z, SoundEvent sound,
						  SoundSource source, float volume, float pitch) {
		level.playSound(except, x, y, z, sound, source, volume, pitch);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return level.getBlockEntity(pos);
	}

	public Level getWrappedLevel() {
		return level;
	}
}
