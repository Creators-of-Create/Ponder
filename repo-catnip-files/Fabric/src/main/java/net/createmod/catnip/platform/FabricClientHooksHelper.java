package net.createmod.catnip.platform;

import java.util.Locale;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.ParticleEngineAccessor;
import io.github.fabricators_of_create.porting_lib.util.client.VertexUtils;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.utility.BasicFluidRenderer;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricClientHooksHelper implements ModClientHooksHelper {
	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getSelected().getJavaLocale();
	}

	@Override
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public void renderBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer, BlockState state, BakedModel model, float red, float green, float blue) {
		dispatcher.getModelRenderer().renderModel(ms.last(), consumer, state, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
	}

	@Override
	public void renderBlockState(BlockRenderDispatcher dispatcher, PoseStack ms, MultiBufferSource buffer, BlockState state) {
		dispatcher.renderSingleBlock(state, ms, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(fluid.getType(), 1000, 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		VertexUtils.putBulkData(consumer, pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public VertexFormat getFormatFromBufferBuilder(BufferBuilder buffer) {
		return buffer.format;
	}

	@Override
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
		int key = Registry.PARTICLE_TYPE.getId(data.getType());
		ParticleProvider<T> particleProvider = (ParticleProvider<T>) ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).port_lib$getProviders().get(key);
		return particleProvider == null ? null : particleProvider.createParticle(data, level, x, y, z, mx, my, mz);
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return Screens.getClient(screen);
	}
}
