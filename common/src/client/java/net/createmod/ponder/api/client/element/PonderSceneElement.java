package net.createmod.ponder.api.client.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.Camera;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface PonderSceneElement extends PonderElement {
	void renderFirst(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue, Camera camera,
					 CameraRenderState cameraRenderState, PoseStack poseStack, float pt);

	void renderLayer(PonderLevel world, MultiBufferSource buffer, ChunkSectionLayer layer, SubmitNodeCollector queue,
	                 Camera camera, CameraRenderState cameraRenderState, PoseStack poseStack, float pt);

	void renderLast(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue, Camera camera,
					CameraRenderState cameraRenderState, PoseStack poseStack, float pt);
}
