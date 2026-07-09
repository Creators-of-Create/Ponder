package net.createmod.catnip.impl.client.gui.element.pip;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.gui.render.pip.GuiBlockModelRenderState;
import net.createmod.catnip.api.client.render.model.BakedModelBufferer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;

public class GuiBlockModelRenderer extends PictureInPictureRenderer<GuiBlockModelRenderState> {
	@Override
	public Class<GuiBlockModelRenderState> getRenderStateClass() {
		return GuiBlockModelRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiBlockModelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		BakedModelBufferer.submitModel(
			Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderState.state()),
			BlockPos.ZERO,
			renderState.state(),
			poseStack,
			(layer, shade) -> {
				throw new UnsupportedOperationException("GUI block model submission should not request direct buffers");
			},
			submitNodeCollector
		);
	}

	@Override
	protected String getTextureLabel() {
		return "catnip:gui_block_model";
	}
}
