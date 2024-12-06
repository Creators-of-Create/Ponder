package net.createmod.catnip.utility.placement;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.createmod.catnip.config.CClient;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.enums.CatnipGuiTextures;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlacementClient {

	static final LerpedFloat angle = LerpedFloat.angular()
		.chase(0, 0.25f, LerpedFloat.Chaser.EXP);
	@Nullable static BlockPos target = null;
	@Nullable static BlockPos lastTarget = null;
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
			for (IPlacementHelper helper : PlacementHelpers.getHelpersView()) {
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
					h.renderAt(pos, state, ray, offset);
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

	public static void onRenderCrosshairOverlay(Window window, GuiGraphics graphics, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if (player != null && animationTick > 0) {
			float screenY = window.getGuiScaledHeight() / 2f;
			float screenX = window.getGuiScaledWidth() / 2f;
			float progress = getCurrentAlpha();

			drawDirectionIndicator(graphics, partialTicks, screenX, screenY, progress);
		}
	}

	public static float getCurrentAlpha() {
		return Math.min(animationTick / 10f/* + event.getPartialTicks() */, 1f);
	}

	private static void drawDirectionIndicator(GuiGraphics graphics, float partialTicks, float centerX, float centerY,
		float progress) {
		float r = .8f;
		float g = .8f;
		float b = .8f;
		float a = progress * progress;

		Vec3 projTarget = VecHelper.projectToPlayerView(VecHelper.getCenterOf(lastTarget), partialTicks);

		Vec3 target = new Vec3(projTarget.x, projTarget.y, 0);
		if (projTarget.z > 0)
			target = target.reverse();

		Vec3 norm = target.normalize();
		Vec3 ref = new Vec3(0, 1, 0);
		float targetAngle = AngleHelper.deg(Math.acos(norm.dot(ref)));

		if (norm.x < 0)
			targetAngle = 360 - targetAngle;

		if (animationTick < 10)
			angle.setValue(targetAngle);

		angle.chase(targetAngle, .25f, LerpedFloat.Chaser.EXP);
		angle.tickChaser();

		float snapSize = 22.5f;
		float snappedAngle = (snapSize * Math.round(angle.getValue(0f) / snapSize)) % 360f;

		float length = 10;

		CClient.PlacementIndicatorSetting mode = CatnipConfig.Client().placementIndicator.get();
		PoseStack poseStack = graphics.pose();
		if (mode == CClient.PlacementIndicatorSetting.TRIANGLE)
			fadedArrow(poseStack, centerX, centerY, r, g, b, a, length, snappedAngle);
		else if (mode == CClient.PlacementIndicatorSetting.TEXTURE)
			textured(poseStack, centerX, centerY, a, snappedAngle);
	}

	private static void fadedArrow(PoseStack ms, float centerX, float centerY, float r, float g, float b, float a,
		float length, float snappedAngle) {
		//RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		ms.pushPose();
		ms.translate(centerX, centerY, 5);
		ms.mulPose(Axis.ZP.rotationDegrees(angle.getValue(0)));
		// RenderSystem.rotatef(snappedAngle, 0, 0, 1);
		double scale = CatnipConfig.Client().indicatorScale.get();
		ms.scale((float) scale, (float) scale, 1);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

		Matrix4f mat = ms.last().pose();

		bufferbuilder.vertex(mat, 0, -(10 + length), 0).color(r, g, b, a).endVertex();

		bufferbuilder.vertex(mat, -9, -3, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, -6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, -3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 0, -8.5f, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 9, -3, 0).color(r, g, b, 0f).endVertex();

		tesselator.end();
		RenderSystem.disableBlend();
		//RenderSystem.enableTexture();
		ms.popPose();
	}

	public static void textured(PoseStack ms, float centerX, float centerY, float alpha, float snappedAngle) {
		//RenderSystem.enableTexture();
		CatnipGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);

		ms.pushPose();
		ms.translate(centerX, centerY, 50);
		float scale = CatnipConfig.Client().indicatorScale.get()
			.floatValue() * .75f;
		ms.scale(scale, scale, 1);
		ms.scale(12, 12, 1);

		float index = snappedAngle / 22.5f;
		float tex_size = 16f / 256f;

		float tx = 0;
		float ty = index * tex_size;
		float tw = 1f;
		float th = tex_size;

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

		Matrix4f mat = ms.last().pose();
		buffer.vertex(mat, -1, -1, 0).color(1f, 1f, 1f, alpha).uv(tx, ty).endVertex();
		buffer.vertex(mat, -1,  1, 0).color(1f, 1f, 1f, alpha).uv(tx, ty + th).endVertex();
		buffer.vertex(mat,  1,  1, 0).color(1f, 1f, 1f, alpha).uv(tx + tw, ty + th).endVertex();
		buffer.vertex(mat,  1, -1, 0).color(1f, 1f, 1f, alpha).uv(tx + tw, ty).endVertex();

		tesselator.end();

		RenderSystem.disableBlend();
		ms.popPose();
	}
}
