package net.createmod.catnip.platform.services;

import java.util.Locale;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface ModClientHooksHelper {

	Locale getCurrentLocale();

	void enableStencilBuffer(RenderTarget renderTarget);

	void renderBlockStateModel(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer, BlockState state, BakedModel model, float red, float green, float blue);

	default void renderBlockStateBatched(BlockRenderDispatcher dispatcher, PoseStack ms, VertexConsumer consumer, BlockState state, BlockPos pos, BlockAndTintGetter level, boolean checkSides, Random random, @Nullable BlockEntity tileWithModelData) {
		dispatcher.renderBatched(state, pos, level, ms, consumer, checkSides, random);
	}

	void renderBlockState(BlockRenderDispatcher dispatcher, PoseStack ms, MultiBufferSource buffer, BlockState state);

	void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid);

	void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay);

	default boolean chunkRenderTypeMatches(BlockState state, RenderType layer) {
		return ItemBlockRenderTypes.getChunkRenderType(state) == layer;
	}

	default boolean fluidRenderTypeMatches(FluidState state, RenderType layer) {
		return ItemBlockRenderTypes.getRenderLayer(state) == layer;
	}

	<T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz);

	Minecraft getMinecraftFromScreen(Screen screen);

	default boolean isKeyPressed(KeyMapping mapping) {
		return mapping.isDown();
	}
}
