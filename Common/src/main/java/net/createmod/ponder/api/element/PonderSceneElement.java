package net.createmod.ponder.api.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public interface PonderSceneElement extends PonderElement {

	void renderFirst(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float pt);

	void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, PoseStack ms, float pt);

	void renderLast(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float pt);

}
