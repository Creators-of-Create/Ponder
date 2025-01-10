package net.createmod.catnip.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;

public interface StencilElement extends RenderElement {

	@Override
	default void render(GuiGraphics graphics) {
		graphics.pose().pushPose();
		transform(graphics);
		prepareStencil(graphics);
		renderStencil(graphics);
		prepareElement(graphics);
		renderElement(graphics);
		cleanUp(graphics);
		graphics.pose().popPose();
	}

	void renderStencil(GuiGraphics graphics);

	void renderElement(GuiGraphics graphics);

	default void transform(GuiGraphics graphics) {
		graphics.pose().translate(getX(), getY(), getZ());
	}

	default void prepareStencil(GuiGraphics graphics) {
		graphics.flush();
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);
	}

	default void prepareElement(GuiGraphics graphics) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	default void cleanUp(GuiGraphics graphics) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		graphics.flush();

	}
}
