package net.createmod.catnip.impl.client.gui.element.pip;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.gui.render.pip.GuiBlockEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class GuiBlockEntityRenderer extends PictureInPictureRenderer<GuiBlockEntityRenderState> {
	@Override
	public Class<GuiBlockEntityRenderState> getRenderStateClass() {
		return GuiBlockEntityRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		CameraRenderState cameraRenderState = new CameraRenderState();

		Minecraft.getInstance().getBlockEntityRenderDispatcher()
			.getRenderer(renderState.blockEntityRenderState())
			.submit(renderState.blockEntityRenderState(), poseStack, submitNodeCollector, cameraRenderState);
	}

	@Override
	protected String getTextureLabel() {
		return "catnip:gui_block_entity";
	}
}
