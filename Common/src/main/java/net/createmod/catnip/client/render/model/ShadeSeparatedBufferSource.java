package net.createmod.catnip.client.render.model;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;

public interface ShadeSeparatedBufferSource {
	VertexConsumer getBuffer(RenderType chunkRenderType, boolean shade);
}
