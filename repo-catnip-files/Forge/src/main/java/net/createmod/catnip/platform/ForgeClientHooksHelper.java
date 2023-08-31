package net.createmod.catnip.platform;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.mixin.client.accessor.ParticleEngineAccessor;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.utility.BasicFluidRenderer;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class ForgeClientHooksHelper implements ModClientHooksHelper {

	private final Map<ResourceLocation, ParticleProvider<?>> particleProviders = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).create$getProviders();

	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getJavaLocale();
	}

	@Override
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public void renderVirtualBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer,
											 BlockState state, BakedModel model, float red, float green, float blue,
											 RenderType layer) {
		dispatcher.getModelRenderer().renderModel(ms.last(), consumer, state, model, red, green, blue,
												  LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
												  ModelUtil.VIRTUAL_DATA, layer);
	}

	@Override
	public void renderBlockStateBatched(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer,
										BlockState state, BlockPos pos, BlockAndTintGetter level, boolean checkSides,
										RandomSource random, RenderType layer, @Nullable BlockEntity BEWithModelData) {
		ModelData modelData = BEWithModelData != null ? BEWithModelData.getModelData() : ModelData.EMPTY;
		modelData = dispatcher.getBlockModel(state).getModelData(level, pos, state, modelData);
		dispatcher.renderBatched(state, pos, level, ms, consumer, checkSides, random, modelData, layer);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(fluid.getType(), 1000, 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT,
										  false);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
												   float red, float green, float blue, float alpha, int packedLight,
												   int packedOverlay) {
		consumer.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay, true);
	}

	public Iterable<RenderType> getRenderTypesForBlockModel(BlockState state, RandomSource random,
															@Nullable BlockEntity BEWithModelData) {
		BakedModel model = ModelUtil.VANILLA_RENDERER.getBlockModel(state);
		ModelData modelData = BEWithModelData != null ? BEWithModelData.getModelData() : ModelData.EMPTY;
		return model.getRenderTypes(state, random, modelData);
	}

	@Override
	public boolean doesBlockModelContainRenderType(RenderType layer, BlockState state, RandomSource random,
												   @Nullable BlockEntity BEWithModelData) {
		BakedModel model = ModelUtil.VANILLA_RENDERER.getBlockModel(state);
		ModelData modelData = BEWithModelData != null ? BEWithModelData.getModelData() : ModelData.EMPTY;
		return model.getRenderTypes(state, random, modelData).contains(layer);
	}

	@Override
	public void renderGuiGameElementModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
										  PoseStack ms, BlockState blockState, BakedModel blockModel, int color) {
		if (blockState.getBlock() == Blocks.AIR) {
			RenderType renderType = Sheets.translucentCullBlockSheet();
			blockRenderer.getModelRenderer().renderModel(ms.last(), buffer.getBuffer(renderType), blockState,
														 blockModel, 1, 1, 1, LightTexture.FULL_BRIGHT,
														 OverlayTexture.NO_OVERLAY, ModelUtil.VIRTUAL_DATA, null);
		} else {
			int blockColor = Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, 0);
			Color rgb = new Color(blockColor == -1 ? color : blockColor);

			for (RenderType chunkType : blockModel.getRenderTypes(blockState, RandomSource.create(42L),
																  ModelUtil.VIRTUAL_DATA)) {
				RenderType renderType = RenderTypeHelper.getEntityRenderType(chunkType, true);
				blockRenderer.getModelRenderer().renderModel(ms.last(), buffer.getBuffer(renderType), blockState,
															 blockModel, rgb.getRedAsFloat(), rgb.getGreenAsFloat(),
															 rgb.getBlueAsFloat(), LightTexture.FULL_BRIGHT,
															 OverlayTexture.NO_OVERLAY, ModelUtil.VIRTUAL_DATA,
															 chunkType);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y,
																	   double z, double mx, double my, double mz) {
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

	@Override
	public BlockRenderDispatcher getBlockRenderDispatcher() {
		return ModelUtil.VANILLA_RENDERER;
	}
}
