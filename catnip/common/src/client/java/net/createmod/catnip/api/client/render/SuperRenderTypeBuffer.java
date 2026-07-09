package net.createmod.catnip.api.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface SuperRenderTypeBuffer extends MultiBufferSource {
	VertexConsumer getEarlyBuffer(RenderType type);

	VertexConsumer getBuffer(RenderType type);

	VertexConsumer getLateBuffer(RenderType type);

	default VertexConsumer getEarlyBuffer(ChunkSectionLayer layer) {
		return getEarlyBuffer(RenderHelper.convertLayerToType(layer));
	}

	default VertexConsumer getBuffer(ChunkSectionLayer layer) {
		return getBuffer(RenderHelper.convertLayerToType(layer));
	}

	default VertexConsumer getLateBuffer(ChunkSectionLayer layer) {
		return getLateBuffer(RenderHelper.convertLayerToType(layer));
	}

	void draw();

	void draw(RenderType type);
}
