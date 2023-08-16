package net.createmod.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.ponder.foundation.PonderLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AnimatedSceneElement extends PonderSceneElement {

	protected Vec3 fadeVec;
	protected LerpedFloat fade;

	public AnimatedSceneElement() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void forceApplyFade(float fade) {
		this.fade.startWithValue(fade);
	}

	public void setFade(float fade) {
		this.fade.setValue(fade);
	}

	public void setFadeVec(Vec3 fadeVec) {
		this.fadeVec = fadeVec;
	}

	@Override
	public final void renderFirst(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderFirst(world, buffer, ms, currentFade, pt);
		ms.popPose();
	}

	@Override
	public final void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, PoseStack ms,
								  float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderLayer(world, buffer, type, ms, currentFade, pt);
		ms.popPose();
	}

	@Override
	public final void renderLast(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderLast(world, buffer, ms, currentFade, pt);
		ms.popPose();
	}

	protected float applyFade(PoseStack ms, float pt) {
		float currentFade = fade.getValue(pt);
		if (fadeVec != null) {
			Vec3 scaled = fadeVec.scale(-1 + currentFade);
			ms.translate(scaled.x, scaled.y, scaled.z);
		}

		return currentFade;
	}

	protected void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, PoseStack ms, float fade,
							   float pt) {}

	protected void renderFirst(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {}

	protected void renderLast(PonderLevel world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {}

	protected int lightCoordsFromFade(float fade) {
		int light = LightTexture.FULL_BRIGHT;
		if (fade != 1) {
			light = (int) (Mth.lerp(fade, 5, 0xF));
			light = LightTexture.pack(light, light);
		}
		return light;
	}

}
