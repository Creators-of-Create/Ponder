package net.createmod.catnip.client.render.model;

import java.util.Iterator;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public final class ForgeBakedModelBufferer {
	private ForgeBakedModelBufferer() {
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, @Nullable ModelData modelData, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, modelData, bufferSource);
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, @Nullable ModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, modelData, resultConsumer);
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, @Nullable Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, modelDataLookup, renderFluids, bufferSource);
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, @Nullable Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, modelDataLookup, renderFluids, resultConsumer);
	}
}
