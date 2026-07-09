package net.createmod.ponder.impl.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.api.client.render.PonderRenderTypes;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.createmod.catnip.api.theme.Color;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.api.client.scene.PonderScene.SceneTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class PonderSceneRenderer extends PictureInPictureRenderer<PonderSceneRenderState> {
	@Override
	protected void renderToTexture(PonderSceneRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		Minecraft mc = Minecraft.getInstance();
		GameRenderer gameRenderer = mc.gameRenderer;

		poseStack.pushPose();
		poseStack.setIdentity();

		renderScene(state, poseStack, submitNodeCollector);

		poseStack.popPose();
	}

	private void renderScene(PonderSceneRenderState state, PoseStack poseStack, SubmitNodeCollector queue) {
		float partialTicks = state.partialTicks();
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		PonderScene scene = state.scene();

		poseStack.pushPose();
		poseStack.translate(0, 0, -800);
		SceneTransform transform = scene.getTransform();
		transform.updateScreenParams(state.width(), state.height(), state.slide(), state.window().guiScale);
		transform.apply(poseStack, partialTicks);
		transform.updateSceneRVE(partialTicks);
		scene.renderScene(buffer, queue, poseStack, partialTicks);
		buffer.draw();

		poseStack.pushPose();

		// kool shadow fx
		if (!scene.shouldHidePlatformShadow()) {
			poseStack.pushPose();
			poseStack.translate(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ());
			UIRenderHelper.flipForGuiRender(poseStack);

			float flash = state.finishingFlash().getValue(partialTicks) * .9f;
			float alpha = flash;
			flash *= flash;
			flash = ((flash * 2) - 1);
			flash *= flash;
			flash = 1 - flash;

			for (int f = 0; f < 4; f++) {
				poseStack.translate(scene.getBasePlateSize(), 0, 0);
				poseStack.pushPose();
				poseStack.translate(0, 0, -1 / 1024f);
				if (flash > 0) {
					poseStack.pushPose();
					poseStack.scale(1, .5f + flash * .75f, 1);
					fillGradient(queue, poseStack, 0, -1, -scene.getBasePlateSize(), 0, new Color(0x00_c6ffc9).getRGB(), new Color(0xaa_c6ffc9).scaleAlpha(alpha).getRGB());
					poseStack.popPose();
				}
				poseStack.translate(0, 0, 2 / 1024f);
				fillGradient(queue, poseStack, 0, 0, -scene.getBasePlateSize(), 4, new Color(0x66_000000).getRGB(), new Color(0x00_000000).getRGB());
				poseStack.popPose();
				poseStack.mulPose(Axis.YP.rotationDegrees(-90));
			}
			poseStack.popPose();
		}

		buffer.draw();

//		// coords for debug
//		if (PonderIndex.editingModeActive() && !userViewMode) {
//			poseStack.scale(-1, -1, 1);
//			poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
//			poseStack.translate(1, -8, -1 / 64f);
//
//			// X AXIS
//			poseStack.pushPose();
//			poseStack.translate(4, -3, 0);
//			poseStack.translate(0, 0, -2 / 1024f);
//			for (int x = 0; x <= bounds.getXSpan(); x++) {
//				poseStack.translate(-16, 0, 0);
//				graphics.text(font, x == bounds.getXSpan() ? "x" : "" + x, 0, 0, 0xFFFFFFFF, false);
//			}
//			poseStack.popPose();
//
//			// Z AXIS
//			poseStack.pushPose();
//			poseStack.scale(-1, 1, 1);
//			poseStack.translate(0, -3, -4);
//			poseStack.mulPose(Axis.YP.rotationDegrees(-90));
//			poseStack.translate(-8, -2, 2 / 64f);
//			for (int z = 0; z <= bounds.getZSpan(); z++) {
//				poseStack.translate(16, 0, 0);
//				graphics.text(font, z == bounds.getZSpan() ? "z" : "" + z, 0, 0, 0xFFFFFFFF, false);
//			}
//			poseStack.popPose();
//
//			// DIRECTIONS
//			poseStack.pushPose();
//			poseStack.translate(bounds.getXSpan() * -8, 0, bounds.getZSpan() * 8);
//			poseStack.mulPose(Axis.YP.rotationDegrees(-90));
//			for (Direction d : Iterate.horizontalDirections) {
//				poseStack.mulPose(Axis.YP.rotationDegrees(90));
//				poseStack.pushPose();
//				poseStack.translate(0, 0, bounds.getZSpan() * 16);
//				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
//				graphics.text(font, d.name().substring(0, 1), 0, 0, 0x66FFFFFF, false);
//				graphics.text(font, "|", 2, 10, 0x44FFFFFF, false);
//				graphics.text(font, ".", 2, 14, 0x22FFFFFF, false);
//				poseStack.popPose();
//			}
//			poseStack.popPose();
//			buffer.draw();
//		}

		poseStack.popPose();
		poseStack.popPose();
	}

	private void fillGradient(
		SubmitNodeCollector queue,
		PoseStack poseStack,
		int x0,
		int y0,
		int x1,
		int y1,
		int col1,
		int col2
	) {
		queue.submitCustomGeometry(poseStack, PonderRenderTypes.gui(), (pose, buffer) -> {
			buffer.addVertex(pose, x0, y0, 0).setColor(col1);
			buffer.addVertex(pose, x0, y1, 0).setColor(col2);
			buffer.addVertex(pose, x1, y1, 0).setColor(col2);
			buffer.addVertex(pose, x1, y0, 0).setColor(col1);
		});
	}

	@Override
	protected String getTextureLabel() {
		return "PonderScene";
	}

	@Override
	public Class<PonderSceneRenderState> getRenderStateClass() {
		return PonderSceneRenderState.class;
	}
}
