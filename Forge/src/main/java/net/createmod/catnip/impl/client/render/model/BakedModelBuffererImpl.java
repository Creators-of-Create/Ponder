package net.createmod.catnip.impl.client.render.model;

import java.util.Iterator;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.VirtualBlockGetter;
import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.impl.client.render.TransformingVertexConsumer;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/BakedModelBufferer.java
public final class BakedModelBuffererImpl {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBuffererImpl() {
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, @Nullable ModelData modelData, ShadeSeparatedBufferSource bufferSource) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		if (modelData == null) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			modelData = blockEntity != null ? blockEntity.getModelData() : (level instanceof VirtualBlockGetter ? VirtualRenderHelper.VIRTUAL_DATA : ModelData.EMPTY);
		}
		RandomSource random = objects.random;
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;

		long seed = state.getSeed(pos);
		modelData = model.getModelData(level, pos, state, modelData);
		random.setSeed(seed);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		ModelBlockRenderer blockRenderer = Minecraft.getInstance()
			.getBlockRenderer()
			.getModelRenderer();

		for (RenderType renderType : renderTypes) {
			universalEmitter.prepare(bufferSource, renderType);
			poseStack.pushPose();
			blockRenderer.tesselateBlock(level, model, state, pos, poseStack, universalEmitter, false, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();
		}

		universalEmitter.clear();
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, @Nullable ModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
		bufferSource.prepare(resultConsumer);
		bufferModel(model, pos, level, state, poseStack, modelData, bufferSource);
		bufferSource.end();
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, @Nullable Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		if (modelDataLookup == null) {
			modelDataLookup = pos -> {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				return blockEntity != null ? blockEntity.getModelData() : (level instanceof VirtualBlockGetter ? VirtualRenderHelper.VIRTUAL_DATA : ModelData.EMPTY);
			};
		}
		RandomSource random = objects.random;
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance()
			.getBlockRenderer();

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

					transformingWrapper.prepare(bufferSource.getBuffer(renderType, true), poseStack);

					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BakedModel model = renderDispatcher.getBlockModel(state);
				ModelData modelData = modelDataLookup.apply(pos);
				modelData = model.getModelData(level, pos, state, modelData);
				random.setSeed(seed);
				ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

				for (RenderType renderType : renderTypes) {
					universalEmitter.prepare(bufferSource, renderType);
					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					blockRenderer.tesselateBlock(level, model, state, pos, poseStack, universalEmitter, true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
					poseStack.popPose();
				}
			}
		}

		ModelBlockRenderer.clearCache();
		transformingWrapper.clear();
		universalEmitter.clear();
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, @Nullable Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
		bufferSource.prepare(resultConsumer);
		bufferBlocks(posIterator, level, poseStack, modelDataLookup, renderFluids, bufferSource);
		bufferSource.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
		public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
