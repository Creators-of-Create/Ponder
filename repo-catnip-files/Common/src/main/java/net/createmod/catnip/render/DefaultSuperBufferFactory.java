package net.createmod.catnip.render;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultSuperBufferFactory implements SuperBufferFactory {

	private static SuperBufferFactory factory = new DefaultSuperBufferFactory();

	public static SuperBufferFactory getInstance() {
		return factory;
	}

	public static void setInstance(SuperBufferFactory factory) {
		DefaultSuperBufferFactory.factory = factory;
	}

	@Override
	public SuperByteBuffer create(BufferBuilder builder) {
		return new DefaultSuperByteBuffer(builder);
	}

	@Override
	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState, PoseStack ms) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormat.BLOCK.getIntegerSize());
		Random random = new Random();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.tesselateBlock(Minecraft.getInstance().level, model, referenceState, BlockPos.ZERO.above(255), ms,
				builder, true, random, 42, OverlayTexture.NO_OVERLAY);
		builder.end();
		return new DefaultSuperByteBuffer(builder);
	}
}
