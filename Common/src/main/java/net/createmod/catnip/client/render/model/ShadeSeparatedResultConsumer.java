package net.createmod.catnip.client.render.model;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

public interface ShadeSeparatedResultConsumer {
	void accept(RenderType renderType, boolean shaded, BufferBuilder.RenderedBuffer data);
}
