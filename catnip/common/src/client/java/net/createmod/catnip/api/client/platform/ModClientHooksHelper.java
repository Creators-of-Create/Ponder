package net.createmod.catnip.api.client.platform;

import java.util.Iterator;
import java.util.Locale;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.api.platform.ServiceHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface ModClientHooksHelper {
	ModClientHooksHelper INSTANCE = ServiceHelper.load(ModClientHooksHelper.class);

	Locale getCurrentLocale();

	@Nullable
	<T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z,
																double mx, double my, double mz);

	Minecraft getMinecraftFromScreen(Screen screen);

	// note: implementations don't use isDown since we need this to work inside screens
	boolean isKeyPressed(KeyMapping mapping);

	void registerPictureInPictureRenderer(Class<?> stateClass, Supplier<PictureInPictureRenderer<?>> factory);

	RenderPipeline.Builder useDrawModeInGui(RenderPipeline.Builder builder);

	void submitFullFluidState(PoseStack ms, OrderedSubmitNodeCollector buffer, FluidState fluid);

	void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid);

	@ApiStatus.Internal
	void submitModel(BlockStateModel model, BlockPos pos, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource, OrderedSubmitNodeCollector submitNodeCollector);

	@ApiStatus.Internal
	void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource);

	@ApiStatus.Internal
	void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedResultConsumer resultConsumer);

	@ApiStatus.Internal
	void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedBufferSource bufferSource);

	@ApiStatus.Internal
	void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer);

	@Deprecated(forRemoval = true)
	default ShadedBlockSbbBuilder createSbbBuilder(BufferBuilder builder) {
		return ShadedBlockSbbBuilder.create();
	}
}
