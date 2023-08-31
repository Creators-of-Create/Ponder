package net.createmod.ponder.foundation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class PonderChapter implements ScreenElement {

	private final ResourceLocation id;
	private final ResourceLocation icon;

	private PonderChapter(ResourceLocation id) {
		this.id = id;
		icon = new ResourceLocation(id.getNamespace(), "textures/ponder/chapter/" + id.getPath() + ".png");
	}

	public ResourceLocation getId() {
		return id;
	}

	public String getTitle() {
		return PonderLocalization.getChapter(id);
	}

	public PonderChapter addTagsToChapter(PonderTag... tags) {
		for (PonderTag t : tags)
			PonderRegistry.TAGS.add(t, this);
		return this;
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		RenderSystem.setShaderTexture(0, icon);
		ms.scale(0.25f, 0.25f, 1);
		//x and y offset, blit z offset, tex x and y, tex width and height, entire tex sheet width and height
		graphics.blit(icon, x, y, 0, 0, 0, 64, 64, 64, 64);
		ms.popPose();
	}

	@Nonnull
	public static PonderChapter of(ResourceLocation id) {
		PonderChapter chapter = PonderRegistry.CHAPTERS.getChapter(id);
		if (chapter == null) {
			 chapter = PonderRegistry.CHAPTERS.addChapter(new PonderChapter(id));
		}

		return chapter;
	}
}
