package net.createmod.catnip.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.ShadeSeparatingSuperByteBuffer;
import net.createmod.catnip.render.TemplateMesh;

public interface ExternalRenderHelper {
	public boolean renderInto(ShadeSeparatingSuperByteBuffer byteBuffer, PoseStack input, VertexConsumer builder);
}
