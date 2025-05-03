package net.createmod.catnip.platform;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.render.BasicFluidRenderer;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.mixin.client.accessor.ParticleEngineAccessor;
import net.createmod.ponder.render.ForgeShadedBlockSbbBuilder;
import net.createmod.ponder.render.VirtualRenderHelper;
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
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;

public class ForgeClientHooksHelper implements ModClientHooksHelper {
	private final Map<ResourceLocation, ParticleProvider<?>> particleProviders = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).ponder$getProviders();

	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getJavaLocale();
	}

	@Override
	@Nullable
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
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(fluid.getType(), 1000, 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false, true);
	}

	@Override
	public void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, null, bufferSource);
	}

	@Override
	public void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, null, resultConsumer);
	}

	@Override
	public void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, null, renderFluids, bufferSource);
	}

	@Override
	public void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, null, renderFluids, resultConsumer);
	}

	//

	@Override
	public ShadedBlockSbbBuilder createSbbBuilder(BufferBuilder builder) {
		return new ForgeShadedBlockSbbBuilder(builder);
	}

	@Override
	public Iterable<RenderType> getRenderTypesForBlockModel(BlockState state, RandomSource random,
															@Nullable BlockEntity beWithModelData) {
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
		ModelData modelData = beWithModelData != null ? beWithModelData.getModelData() : ModelData.EMPTY;
		return model.getRenderTypes(state, random, modelData);
	}

	@Override
	public boolean doesBlockModelContainRenderType(RenderType layer, BlockState state, RandomSource random,
												   @Nullable BlockEntity beWithModelData) {
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
		ModelData modelData = beWithModelData != null ? beWithModelData.getModelData() : ModelData.EMPTY;
		return model.getRenderTypes(state, random, modelData).contains(layer);
	}

	@Override
	public void tesselateBlockVirtual(BlockRenderDispatcher dispatcher, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource randomSource, long seed, int packedOverlay, @Nullable RenderType renderType) {
		ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
		ModelData modelData = model.getModelData(EmptyVirtualBlockGetter.FULL_DARK, pos, state, VirtualRenderHelper.VIRTUAL_DATA);
		modelRenderer.tesselateBlock(EmptyVirtualBlockGetter.FULL_DARK, model, state, pos, poseStack, consumer, checkSides, randomSource, seed, packedOverlay, modelData, renderType);
	}

	@Override
	public void tesselateBlockVirtual(Level level, BlockRenderDispatcher dispatcher, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource randomSource, long seed, int packedOverlay, @Nullable RenderType renderType) {
		ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		ModelData modelData = model.getModelData(level, pos, state, blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData());
		modelRenderer.tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, randomSource, seed, packedOverlay, modelData, renderType);
	}

	@Override
	public void renderGuiGameElementModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
										  PoseStack ms, BlockState blockState, BakedModel blockModel, int color, @Nullable BlockEntity beWithModelData) {
		ModelData modelData = beWithModelData != null ? beWithModelData.getModelData() : VirtualRenderHelper.VIRTUAL_DATA;

		if (blockState.getBlock() == Blocks.AIR) {
			RenderType renderType = Sheets.translucentCullBlockSheet();
			blockRenderer.getModelRenderer().renderModel(ms.last(), buffer.getBuffer(renderType), blockState,
														 blockModel, 1, 1, 1, LightTexture.FULL_BRIGHT,
														 OverlayTexture.NO_OVERLAY, modelData, null);
		} else {
			int blockColor = Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, 0);
			Color rgb = new Color(blockColor == -1 ? color : blockColor);

			for (RenderType chunkType : blockModel.getRenderTypes(blockState, RandomSource.create(42L),
																  VirtualRenderHelper.VIRTUAL_DATA)) {
				RenderType renderType = RenderTypeHelper.getEntityRenderType(chunkType, true);
				blockRenderer.getModelRenderer().renderModel(ms.last(), buffer.getBuffer(renderType), blockState,
															 blockModel, rgb.getRedAsFloat(), rgb.getGreenAsFloat(),
															 rgb.getBlueAsFloat(), LightTexture.FULL_BRIGHT,
															 OverlayTexture.NO_OVERLAY, modelData, chunkType);
			}
		}
	}

	@Override
	public void renderVirtualBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer,
											 BlockState state, BakedModel model, float red, float green, float blue,
											 RenderType layer) {
		dispatcher.getModelRenderer().renderModel(ms.last(), consumer, state, model, red, green, blue,
			LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
			VirtualRenderHelper.VIRTUAL_DATA, layer);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
												   float red, float green, float blue, float alpha, int packedLight,
												   int packedOverlay) {
		consumer.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay, true);
	}
}
