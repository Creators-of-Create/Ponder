package net.createmod.catnip.platform;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.baked.VirtualEmptyBlockGetter;
import io.github.fabricators_of_create.porting_lib.mixin.accessors.client.accessor.ParticleEngineAccessor;
import io.github.fabricators_of_create.porting_lib.models.virtual.FixedColorTintingBakedModel;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.utility.BasicFluidRenderer;
import net.createmod.catnip.utility.VertexUtils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricClientHooksHelper implements ModClientHooksHelper {
	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getSelectedJavaLocale();
	}

	@Override
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public void renderVirtualBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer,
											 BlockState state, BakedModel model, float red, float green, float blue,
											 RenderType layer) {
		//BakedModel wrappedModel = DefaultLayerFilteringBakedModel.wrap(model);
		dispatcher.getModelRenderer().renderModel(ms.last(), consumer, state, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
	}

	@Override
	public void tesselateBlockVirtual(BlockRenderDispatcher dispatcher, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource randomSource, long seed, int packedOverlay, RenderType renderType) {

	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(fluid.getType(), 1000, 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false, true);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		VertexUtils.putBulkData(consumer, pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public Iterable<RenderType> getRenderTypesForBlockModel(BlockState state, RandomSource random,
															@Nullable BlockEntity BEWithModelData) {
		return List.of(ItemBlockRenderTypes.getRenderType(state, false));
	}

	@Override
	public boolean doesBlockModelContainRenderType(RenderType layer, BlockState state, RandomSource random,
												   BlockEntity BEWithModelData) {
		return ItemBlockRenderTypes.getChunkRenderType(state) == layer;
	}

	@Override
	public void renderGuiGameElementModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
										  PoseStack ms, BlockState state, BakedModel blockModel, int color, BlockEntity BEwithModelData) {
		int blockColor = Minecraft.getInstance()
				.getBlockColors()
				.getColor(state, null, null, 0);
//			Color rgb = new Color(color == -1 ? this.color : color);
//			blockRenderer.getModelRenderer()
//				.renderModel(ms.last(), vb, blockState, blockModel, rgb.getRedAsFloat(), rgb.getGreenAsFloat(), rgb.getBlueAsFloat(),
//					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		BakedModel model = blockModel;
		//model = DefaultLayerFilteringBakedModel.wrap(model);
		if (blockColor == -1) {
			blockColor = color;
		}
		if (blockColor != -1) {
			model = FixedColorTintingBakedModel.wrap(model, blockColor);
		}

		RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
		blockRenderer.getModelRenderer()
				.tesselateBlock(VirtualEmptyBlockGetter.FULL_BRIGHT, model, state, BlockPos.ZERO, ms, buffer.getBuffer(
						renderType), false, RandomSource.create(), 42L, OverlayTexture.NO_OVERLAY);

	}

	@Override
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
		int key = BuiltInRegistries.PARTICLE_TYPE.getId(data.getType());
		ParticleProvider<T> particleProvider = (ParticleProvider<T>) ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).port_lib$getProviders().get(key);
		return particleProvider == null ? null : particleProvider.createParticle(data, level, x, y, z, mx, my, mz);
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return Screens.getClient(screen);
	}

	@Override
	public boolean isKeyPressed(KeyMapping mapping) {
		int keyCode = KeyBindingHelper.getBoundKeyOf(mapping).getValue();
		long window = Minecraft.getInstance().getWindow().getWindow();
		return InputConstants.isKeyDown(window, keyCode);
	}
}
