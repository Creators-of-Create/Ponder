package net.createmod.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.ponder.api.element.AnimatedSceneElement;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AnimatedSceneElementBase extends PonderElementBase implements AnimatedSceneElement {

	protected Vec3 fadeVec;
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
	public void setFadeVec(Vec3 fadeVec) {
		this.fadeVec = fadeVec;
	}

	@Override
	public final void renderFirst(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float pt) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderFirst(world, buffer, graphics, currentFade, pt);
		poseStack.popPose();
	}

	@Override
	public final void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, GuiGraphics graphics,
								  float pt) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderLayer(world, buffer, type, graphics, currentFade, pt);
		poseStack.popPose();
	}

	@Override
	public final void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float pt) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		float currentFade = applyFade(poseStack, pt);
		renderLast(world, buffer, graphics, currentFade, pt);
		poseStack.popPose();
	}

	protected float applyFade(PoseStack ms, float pt) {
		float currentFade = fade.getValue(pt);
		if (fadeVec != null) {
			Vec3 scaled = fadeVec.scale(-1 + currentFade);
			ms.translate(scaled.x, scaled.y, scaled.z);
		}

		return currentFade;
	}

	protected void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, GuiGraphics graphics, float fade,
							   float pt) {}

	protected void renderFirst(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {}

	protected void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {}

	protected int lightCoordsFromFade(float fade) {
		int light = LightTexture.FULL_BRIGHT;
		if (fade != 1) {
			light = (int) (Mth.lerp(fade, 5, 0xF));
			light = LightTexture.pack(light, light);
		}
		return light;
	}

}
