package net.createmod.catnip.impl.neoforge.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.api.client.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.createmod.catnip.impl.neoforge.render.BakedModelBuffererImpl;
import net.createmod.catnip.impl.neoforge.render.NeoForgeShadedBlockSbbBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class NeoForgeClientHooksHelper implements ModClientHooksHelper {
	private static final Supplier<Map<Identifier, ParticleProvider<?>>> particleProviders = () -> Minecraft.getInstance().particleEngine.resourceManager.getProviders();

	@Internal
	public static final Map<Class<?>, Supplier<PictureInPictureRenderer<?>>> PIP_RENDERERS = new HashMap<>();

	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getJavaLocale();
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y,
																	   double z, double mx, double my, double mz) {
		Identifier key = RegisteredObjectsHelper.getKeyOrThrow(data.getType());
		ParticleProvider<T> particleProvider = (ParticleProvider<T>) particleProviders.get().get(key);
		return particleProvider == null ? null : particleProvider.createParticle(data, level, x, y, z, mx, my, mz, level.getRandom());
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return screen.getMinecraft();
	}

	@Override
	public boolean isKeyPressed(KeyMapping mapping) {
		int keyCode = mapping.getKey().getValue();
		Window window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, keyCode) && mapping.isConflictContextAndModifierActive();
	}

	@Override
	public void registerPictureInPictureRenderer(Class<?> stateClass, Supplier<PictureInPictureRenderer<?>> factory) {
		PIP_RENDERERS.put(stateClass, factory);
	}

	@Override
	public Builder useDrawModeInGui(Builder builder) {
		return builder;
	}

	@Override
	public void submitFullFluidState(PoseStack ms, OrderedSubmitNodeCollector buffer, FluidState fluid) {
		FluidRenderHelper.submitFluidBox(fluid, 0, 0, 0, 1, 1, 1, buffer, ms, LightCoordsUtil.FULL_BRIGHT, false, true);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		FluidRenderHelper.renderFluidBox(fluid, 0, 0, 0, 1, 1, 1, buffer, ms,
			LightCoordsUtil.FULL_BRIGHT, false, true);
	}

	@Override
	public void submitModel(BlockStateModel model, BlockPos pos, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource, OrderedSubmitNodeCollector submitNodeCollector) {
		BakedModelBuffererImpl.submitModel(model, pos, state, poseStack, bufferSource, submitNodeCollector);
	}

	@Override
	public void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, bufferSource);
	}

	@Override
	public void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, resultConsumer);
	}

	@Override
	public void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
	}

	@Override
	public void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, resultConsumer);
	}

	//

	@Override
	public ShadedBlockSbbBuilder createSbbBuilder(BufferBuilder builder) {
		return new NeoForgeShadedBlockSbbBuilder();
	}
}
