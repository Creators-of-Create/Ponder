package net.createmod.catnip.platform;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.mixin.client.accessor.ParticleEngineAccessor;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.utility.BasicFluidRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

public class ForgeClientHooksHelper implements ModClientHooksHelper {

	private final Map<ResourceLocation, ParticleProvider<?>> particleProviders = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).create$getProviders();

	@Override
	public Locale getCurrentLocale() {
		return MinecraftForgeClient.getLocale();
	}

	@Override
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public void renderBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer, BlockState state, BakedModel model, float red, float green, float blue) {
		dispatcher.getModelRenderer().renderModel(ms.last(), consumer, state, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
	}

	@Override
	public void renderBlockStateBatched(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer, BlockState state, BlockPos pos, BlockAndTintGetter level, boolean checkSides, Random random, @Nullable BlockEntity tileWithModelData) {
		dispatcher.renderBatched(state, pos, level, ms, consumer, checkSides, random, tileWithModelData != null ? tileWithModelData.getModelData() : EmptyModelData.INSTANCE);
	}

	@Override
	public void renderBlockState(BlockRenderDispatcher dispatcher, PoseStack ms, MultiBufferSource buffer, BlockState state) {
		dispatcher.renderSingleBlock(state, ms, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(new FluidStack(fluid.getType(), 1000), 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		consumer.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public boolean chunkRenderTypeMatches(BlockState state, RenderType layer) {
		return ItemBlockRenderTypes.canRenderInLayer(state, layer);
	}

	@Override
	public boolean fluidRenderTypeMatches(FluidState state, RenderType layer) {
		return ItemBlockRenderTypes.canRenderInLayer(state, layer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
		ResourceLocation key = CatnipServices.REGISTRIES.getKeyOrThrow(data.getType());
		ParticleProvider<T> particleProvider = (ParticleProvider<T>) particleProviders.get(key);
		return particleProvider == null ? null : particleProvider.createParticle(data, level, x, y, z, mx, my, mz);
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return screen.getMinecraft();
	}

	@Override
	public boolean isKeyPressed(KeyMapping mapping) {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), mapping.getKey().getValue());
	}
}
