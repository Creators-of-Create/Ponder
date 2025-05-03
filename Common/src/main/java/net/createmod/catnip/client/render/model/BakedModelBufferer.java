package net.createmod.catnip.client.render.model;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class BakedModelBufferer {
	private BakedModelBufferer() {
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedBufferSource bufferSource) {
		CatnipClientServices.CLIENT_HOOKS.bufferModel(model, pos, level, state, poseStack, bufferSource);
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ShadeSeparatedResultConsumer resultConsumer) {
		CatnipClientServices.CLIENT_HOOKS.bufferModel(model, pos, level, state, poseStack, resultConsumer);
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedBufferSource bufferSource) {
		CatnipClientServices.CLIENT_HOOKS.bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ShadeSeparatedResultConsumer resultConsumer) {
		CatnipClientServices.CLIENT_HOOKS.bufferBlocks(posIterator, level, poseStack, renderFluids, resultConsumer);
	}
}
