package net.createmod.catnip.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.platform.services.ExternalRenderHelper;
import net.createmod.catnip.render.ShadeSeparatingSuperByteBuffer;
import net.createmod.catnip.render.TemplateMesh;

public class FabricExternalRenderHelper implements ExternalRenderHelper {
	@Override
	public boolean renderInto(ShadeSeparatingSuperByteBuffer byteBuffer, PoseStack input, VertexConsumer builder) {
		return false;
	}
}
