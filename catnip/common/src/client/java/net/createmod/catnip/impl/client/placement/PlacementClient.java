package net.createmod.catnip.impl.client.placement;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import com.mojang.math.Constants;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.gui.render.FadedArrowRenderState;
import net.createmod.catnip.api.client.gui.render.TexturedArrowRenderState;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.placement.PlacementAssistConfig;
import net.createmod.catnip.api.client.placement.PlacementHelperRenderer;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.placement.IPlacementHelper;
import net.createmod.catnip.api.placement.PlacementHelpers;
import net.createmod.catnip.api.placement.PlacementOffset;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlacementClient {
	static final LerpedFloat angle = LerpedFloat.angular()
		.chase(0, 0.25f, LerpedFloat.Chaser.EXP);
	@Nullable
	static BlockPos target = null;
	@Nullable
	static BlockPos lastTarget = null;
	static int animationTick = 0;

	public static void tick() {
		setTarget(null);
		checkHelpers();

		if (target == null) {
			if (animationTick > 0)
				animationTick = Math.max(animationTick - 2, 0);

			return;
		}

		if (animationTick < 10)
			animationTick++;

	}

	private static void checkHelpers() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;

		if (world == null)
			return;

		if (!(mc.hitResult instanceof BlockHitResult ray))
			return;

		if (mc.player == null)
			return;

		if (mc.player.isShiftKeyDown())// for now, disable all helpers when sneaking TODO add helpers that respect
			// sneaking but still show position
			return;

		for (InteractionHand hand : InteractionHand.values()) {

			ItemStack heldItem = mc.player.getItemInHand(hand);

			List<IPlacementHelper> filteredForHeldItem = new ArrayList<>();
			for (IPlacementHelper helper : PlacementHelpers.get()) {
				if (helper.matchesItem(heldItem))
					filteredForHeldItem.add(helper);
			}

			if (filteredForHeldItem.isEmpty())
				continue;

			BlockPos pos = ray.getBlockPos();
			BlockState state = world.getBlockState(pos);

			List<IPlacementHelper> filteredForState = new ArrayList<>();
			for (IPlacementHelper helper : filteredForHeldItem) {
				if (helper.matchesState(state))
					filteredForState.add(helper);
			}

			if (filteredForState.isEmpty())
				continue;

			boolean atLeastOneMatch = false;
			for (IPlacementHelper h : filteredForState) {
				PlacementOffset offset = h.getOffset(mc.player, world, state, pos, ray, heldItem);

				if (offset.isSuccessful()) {
					PlacementHelperRenderer.get(h).render(h, pos, state, ray, offset);
					setTarget(offset.getBlockPos());
					atLeastOneMatch = true;
					break;
				}

			}

			// at least one helper activated, no need to check the offhand if we are still
			// in the mainhand
			if (atLeastOneMatch)
				return;

		}
	}

	static void setTarget(@Nullable BlockPos target) {
		PlacementClient.target = target;

		if (target == null)
			return;

		if (lastTarget == null) {
			lastTarget = target;
			return;
		}

		if (!lastTarget.equals(target))
			lastTarget = target;
	}

	public static void renderCrosshairOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if (player != null && animationTick > 0) {
			float screenY = graphics.guiHeight() / 2f;
			float screenX = graphics.guiWidth() / 2f;
			float progress = getCurrentAlpha();

			drawDirectionIndicator(graphics, deltaTracker, screenX, screenY, progress);
		}
	}

	public static float getCurrentAlpha() {
		return Math.min(animationTick / 10f/* + event.getPartialTicks() */, 1f);
	}

	private static void drawDirectionIndicator(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, float centerX, float centerY, float progress) {
		float r = .8f;
		float g = .8f;
		float b = .8f;
		float a = progress * progress;

		Vec3 projTarget = projectToPlayerView(VecHelper.getCenterOf(lastTarget), deltaTracker.getRealtimeDeltaTicks());

		Vec3 target = new Vec3(projTarget.x, projTarget.y, 0);
		if (projTarget.z > 0)
			target = target.reverse();

		Vec3 norm = target.normalize();
		Vec3 ref = new Vec3(0, 1, 0);
		float targetAngle = AngleHelper.deg(-Math.acos(norm.dot(ref)));

		if (norm.x < 0)
			targetAngle = 360 - targetAngle;

		if (animationTick < 10)
			angle.setValue(targetAngle);

		angle.chase(targetAngle, .25f, LerpedFloat.Chaser.EXP);
		angle.tickChaser();

		float snapSize = 22.5f;
		float snappedAngle = (snapSize * Math.round(angle.getValue(0f) / snapSize)) % 360f;

		float length = 10;

		PlacementAssistConfig.IndicatorSetting mode = PlacementAssistConfig.get().placementIndicator();
		if (mode == PlacementAssistConfig.IndicatorSetting.TRIANGLE) {
			fadedArrow(graphics, centerX, centerY, r, g, b, a, length);
		} else if (mode == PlacementAssistConfig.IndicatorSetting.TEXTURE) {
			textured(graphics, centerX, centerY, a, snappedAngle);
		}
	}

	private static void fadedArrow(GuiGraphicsExtractor graphics, float centerX, float centerY, float r, float g, float b, float a, float length) {
		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(centerX, centerY);
		poseStack.rotate(angle.getValue(0) * Constants.DEG_TO_RAD);
		double scale = PlacementAssistConfig.get().indicatorScale();
		poseStack.scale((float) scale, (float) scale);

		int size = (int) ((10 + length) * scale);
		graphics.guiRenderState.addGuiElement(new FadedArrowRenderState(
			new Matrix3x2f(graphics.pose()), size, length, r, g, b, a
		));

		poseStack.popMatrix();
	}

	public static void textured(GuiGraphicsExtractor graphics, float centerX, float centerY, float alpha, float snappedAngle) {
		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(centerX, centerY);
		float scale = PlacementAssistConfig.get().indicatorScale() * .75f;
		poseStack.scale(scale, scale);
		poseStack.scale(12, 12);

		float index = snappedAngle / 22.5f;
		float texSize = 16f / 256f;

		float tx = 0;
		float ty = index * texSize;
		float tw = 1;
		float th = texSize;

		int size = (int) (36 * scale);
		graphics.guiRenderState.addGuiElement(new TexturedArrowRenderState(
			new Matrix3x2f(graphics.pose()),
			CatnipGuiTextures.PLACEMENT_INDICATOR_SHEET.bind(),
			size,
			alpha,
			tx,
			ty,
			tw,
			th
		));

		poseStack.popMatrix();
	}

	// https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/?do=findComment&comment=413573
	// slightly modified
	private static Vec3 projectToPlayerView(Vec3 target, float partialTicks) {
		/*
		 * The (centered) location on the screen of the given 3d point in the world.
		 * Result is (dist right of center screen, dist up from center screen, if < 0,
		 * then in front of view plane)
		 */
		Camera ari = Minecraft.getInstance().gameRenderer.mainCamera();
		Vec3 cameraPos = ari.position();
		Quaternionf cameraRotationConj = new Quaternionf(ari.rotation());
		cameraRotationConj.conjugate();

		Vector3f result3f = new Vector3f((float) (cameraPos.x - target.x), (float) (cameraPos.y - target.y),
			(float) (cameraPos.z - target.z));
		result3f.rotate(cameraRotationConj);

		// ----- compensate for view bobbing (if active) -----
		// the following code adapted from GameRenderer::applyBobbing (to invert it)
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.bobView().get()) {
			Entity renderViewEntity = mc.getCameraEntity();
			if (renderViewEntity instanceof LocalPlayer playerEntity) {
				ClientAvatarState avatarState = playerEntity.avatarState();
				float f = avatarState.getBackwardsInterpolatedWalkDistance(partialTicks);
				float f1 = avatarState.getInterpolatedBob(partialTicks);
				Quaternionf q2 =
					com.mojang.math.Axis.XP.rotationDegrees(Math.abs(Mth.cos(f * (float) Math.PI - 0.2F) * f1) * 5.0F);
				q2.conjugate();
				result3f.rotate(q2);

				Quaternionf q1 =
					com.mojang.math.Axis.ZP.rotationDegrees(Mth.sin(f * (float) Math.PI) * f1 * 3.0F);
				q1.conjugate();
				result3f.rotate(q1);

				Vector3f bob_translation = new Vector3f((Mth.sin(f * (float) Math.PI) * f1 * 0.5F),
					(-Math.abs(Mth.cos(f * (float) Math.PI) * f1)), 0.0f);
				bob_translation.set(bob_translation.x(), -bob_translation.y(), bob_translation.z()); // this is weird but hey, if it works
				result3f.add(bob_translation);
			}
		}

		// ----- adjust for fov -----
		float fov = ari.getFov();

		float half_height = (float) mc.getWindow()
			.getGuiScaledHeight() / 2;
		float scale_factor = half_height / (result3f.z() * (float) Math.tan(Math.toRadians(fov / 2)));
		return new Vec3(-result3f.x() * scale_factor, result3f.y() * scale_factor, result3f.z());
	}
}
