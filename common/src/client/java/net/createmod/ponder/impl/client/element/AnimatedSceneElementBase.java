package net.createmod.ponder.impl.client.element;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.ponder.api.client.element.AnimatedSceneElement;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.Camera;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AnimatedSceneElementBase extends PonderElementBase implements AnimatedSceneElement {
	protected @Nullable Vec3 fadeVec;
	protected LerpedFloat fade;

	public AnimatedSceneElementBase() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	@Override
	public void forceApplyFade(float fade) {
		this.fade.startWithValue(fade);
	}

	@Override
	public void setFade(float fade) {
		this.fade.setValue(fade);
	}

	@Override
	public void setFadeVec(@Nullable Vec3 fadeVec) {
		this.fadeVec = fadeVec;
	}

	@Override
	public final void renderLayer(PonderLevel world, MultiBufferSource buffer, ChunkSectionLayer layer,
								  SubmitNodeCollector queue, Camera camera, CameraRenderState cameraRenderState,
								  PoseStack poseStack, float pt) {
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderLayer(world, buffer, layer, queue, camera, cameraRenderState, poseStack, currentFade, pt);
		poseStack.popPose();
	}

	@Override
	public final void renderFirst(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue,
								  Camera camera, CameraRenderState cameraRenderState, PoseStack poseStack, float pt) {
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderFirst(world, buffer, queue, camera, cameraRenderState, poseStack, currentFade, pt);
		poseStack.popPose();
	}

	@Override
	public final void renderLast(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue, Camera camera,
								 CameraRenderState cameraRenderState, PoseStack poseStack, float pt) {
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderLast(world, buffer, queue, camera, cameraRenderState, poseStack, currentFade, pt);
		poseStack.popPose();
	}

	protected float applyFade(PoseStack poseStack, float pt) {
		float currentFade = fade.getValue(pt);
		if (fadeVec != null) {
			Vec3 scaled = fadeVec.scale(-1 + currentFade);
			poseStack.translate(scaled.x, scaled.y, scaled.z);
		}

		return currentFade;
	}

	protected void renderLayer(PonderLevel world, MultiBufferSource buffer, ChunkSectionLayer layer,
							   SubmitNodeCollector queue, Camera camera, CameraRenderState cameraRenderState,
							   PoseStack poseStack, float fade, float pt) {
	}

	protected void renderFirst(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue, Camera camera,
							   CameraRenderState cameraRenderState, PoseStack poseStack, float fade, float pt) {
	}

	protected void renderLast(PonderLevel world, MultiBufferSource buffer, SubmitNodeCollector queue, Camera camera,
							  CameraRenderState cameraRenderState, PoseStack poseStack, float fade, float pt) {
	}

	protected int lightCoordsFromFade(float fade) {
		int light = LightCoordsUtil.FULL_BRIGHT;
		if (fade != 1) {
			light = (int) (Mth.lerp(fade, 5, 0xF));
			light = LightCoordsUtil.pack(light, light);
		}
		return light;
	}
}
