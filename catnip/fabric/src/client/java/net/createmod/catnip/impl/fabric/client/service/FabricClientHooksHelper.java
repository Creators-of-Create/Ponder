package net.createmod.catnip.impl.fabric.client.service;

import java.util.Iterator;
import java.util.Locale;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.impl.fabric.client.mixin.ParticleEngineAccessor;
import net.createmod.catnip.impl.fabric.client.render.BakedModelBuffererImpl;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
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
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricClientHooksHelper implements ModClientHooksHelper {
	@Override
	public Locale getCurrentLocale() {
		LanguageManager languageManager = Minecraft.getInstance().getLanguageManager();
		String[] split = languageManager.getSelected().split("_", 2);
		return split.length == 1 ? new Locale(split[0]) : new Locale(split[0], split[1]);
	}

	@Override
	@Nullable
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
		return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).catnip$makeParticle(data, x, y, z, mx, my, mz);
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return Screens.getMinecraft(screen);
	}

	@Override
	public boolean isKeyPressed(KeyMapping mapping) {
		int keyCode = KeyMappingHelper.getBoundKeyOf(mapping).getValue();
		Window window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, keyCode);
	}

	@Override
	public void registerPictureInPictureRenderer(Class<?> stateClass, Supplier<PictureInPictureRenderer<?>> factory) {
		PictureInPictureRendererRegistry.register(ctx -> factory.get());
	}

	@Override
	public Builder useDrawModeInGui(Builder builder) {
		return builder.withUsePipelineDrawModeForGui(true);
	}

	@Override
	public void submitFullFluidState(PoseStack ms, OrderedSubmitNodeCollector submitNode, FluidState fluid) {
		FluidRenderHelper.submitFluidBox(fluid, 0, 0, 0, 1, 1, 1, submitNode, ms, LightCoordsUtil.FULL_BRIGHT, false, true);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		FluidRenderHelper.renderFluidBox(fluid, 0, 0, 0, 1, 1, 1, buffer, ms, LightCoordsUtil.FULL_BRIGHT, false, true);
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
}
