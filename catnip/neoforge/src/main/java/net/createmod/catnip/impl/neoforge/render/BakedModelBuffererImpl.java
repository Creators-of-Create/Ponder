package net.createmod.catnip.impl.neoforge.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.api.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.api.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.impl.client.render.TransformingVertexConsumer;
import net.createmod.catnip.impl.client.render.model.DefaultShadeSeparatedBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/BakedModelBufferer.java
public final class BakedModelBuffererImpl {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
	private static final BlockModelLighter lighter = new BlockModelLighter();

	private BakedModelBuffererImpl() {
	}

	public static void submitModel(BlockStateModel model, BlockPos pos, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource, OrderedSubmitNodeCollector submitNodeCollector) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;

		long seed = state.getSeed(pos);

		universalEmitter.prepare(bufferSource, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT);

		RenderType layer = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
		List<BlockStateModelPart> parts = new ArrayList<>();

		model.collectParts(RandomSource.create(seed), parts);

		poseStack.pushPose();
		submitNodeCollector.submitBlockModel(
			poseStack, layer, parts,
			BlockModelRenderState.EMPTY_TINTS,
			LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0
		);
		poseStack.popPose();

		universalEmitter.clear();
	}

	public static void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;

		long seed = state.getSeed(pos);

		ChunkSectionLayer defaultLayer = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT;
		universalEmitter.prepare(bufferSource, defaultLayer);

		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(seed), parts);

		poseStack.pushPose();

		VertexConsumer buffer = bufferSource.getBuffer(defaultLayer, false);

		QuadInstance instance = new QuadInstance();
		boolean useAo = Minecraft.getInstance().gameRenderer.gameRenderState().optionsRenderState.ambientOcclusion;
		int light = LightCoordsUtil.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));

		instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

		for (Direction direction : Direction.values()) {
			for (BlockStateModelPart part : parts) {
				for (BakedQuad quad : part.getQuads(direction)) {
					if (useAo)
						lighter.prepareQuadAmbientOcclusion(level, state, pos, quad, instance);
					else
						lighter.prepareQuadFlat(level, state, pos, light, quad, instance);

					buffer.putBakedQuad(poseStack.last(), quad, instance);
				}
			}
		}

		for (BlockStateModelPart part : parts) {
			for (BakedQuad quad : part.getQuads(null)) {
				if (useAo)
					lighter.prepareQuadAmbientOcclusion(level, state, pos, quad, instance);
				else
					lighter.prepareQuadFlat(level, state, pos, light, quad, instance);

				buffer.putBakedQuad(poseStack.last(), quad, instance);
			}
		}

		poseStack.popPose();

		universalEmitter.clear();
	}

	public static void bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
		bufferSource.prepare(resultConsumer);
		bufferModel(model, pos, level, state, poseStack, bufferSource);
		bufferSource.end();
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		BlockStateModelSet blockStateModelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
		FluidStateModelSet fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
		FluidRenderer fluidRenderer = new FluidRenderer(fluidStateModelSet);

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));

					PoseStack finalPoseStack = poseStack;

					fluidRenderer.tesselate(level, pos, layer -> {
						transformingWrapper.prepare(bufferSource.getBuffer(layer, true), finalPoseStack);
						return transformingWrapper;
					}, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BlockStateModel model = blockStateModelSet.get(state);

				ChunkSectionLayer defaultLayer = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT) ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT;
				transformingWrapper.prepare(bufferSource.getBuffer(defaultLayer, true), poseStack);
				universalEmitter.prepare(bufferSource, defaultLayer);

				poseStack.pushPose();
				poseStack.translate(pos.getX() * 0.5, pos.getY() * 0.5, pos.getZ() * 0.5);

				List<BlockStateModelPart> parts = new ArrayList<>();
				model.collectParts(RandomSource.create(seed), parts);

				QuadInstance instance = new QuadInstance();
				boolean useAo = Minecraft.getInstance().gameRenderer.gameRenderState().optionsRenderState.ambientOcclusion;
				int light = LightCoordsUtil.getLightCoords(level, pos);

				instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

				for (Direction direction : Direction.values()) {
					for (BlockStateModelPart part : parts) {
						for (BakedQuad quad : part.getQuads(direction)) {
							if (useAo)
								lighter.prepareQuadAmbientOcclusion(level, state, pos, quad, instance);
							else
								lighter.prepareQuadFlat(level, state, pos, light, quad, instance);

							transformingWrapper.putBakedQuad(poseStack.last(), quad, instance);
						}
					}
				}

				for (BlockStateModelPart part : parts) {
					for (BakedQuad quad : part.getQuads(null)) {
						if (useAo)
							lighter.prepareQuadAmbientOcclusion(level, state, pos, quad, instance);
						else
							lighter.prepareQuadFlat(level, state, pos, light, quad, instance);

						transformingWrapper.putBakedQuad(poseStack.last(), quad, instance);
					}
				}

				poseStack.popPose();
			}
		}

		transformingWrapper.clear();
		universalEmitter.clear();
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
		bufferSource.prepare(resultConsumer);
		bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
		bufferSource.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();

		public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
		public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
