package net.createmod.ponder.enums;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.render.ColoredRenderable;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.Ponder;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public enum PonderGuiTextures implements TextureSheetSegment, ScreenElement, ColoredRenderable {

	//widgets
	SPEECH_TOOLTIP_BACKGROUND("widgets", 0, 24, 8, 8),
	SPEECH_TOOLTIP_COLOR("widgets", 8, 24, 8, 8),

	//icons
	ICON_PONDER_LEFT("widgets", 0, 2),
	ICON_PONDER_CLOSE("widgets", 1, 2),
	ICON_PONDER_RIGHT("widgets", 2, 2),
	ICON_PONDER_IDENTIFY("widgets", 3, 2),
	ICON_PONDER_REPLAY("widgets", 4, 2),
	ICON_PONDER_USER_MODE("widgets", 5, 2),
	ICON_PONDER_SLOW_MODE("widgets", 6, 2),

	ICON_LMB("widgets", 0, 4),
	ICON_SCROLL("widgets", 1, 4),
	ICON_RMB("widgets", 2, 4),

	;

	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;

	PonderGuiTextures(String location, int iconColumn, int iconRow) {
		this(location, iconColumn * 16, iconRow * 16, 16, 16);
	}

	PonderGuiTextures(String location, int startX, int startY, int width, int height) {
		this(Ponder.MOD_ID, location, startX, startY, width, height);
	}

	PonderGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@Override
	public void render(PoseStack ms, int x, int y) {
		bind();
		GuiComponent.blit(ms, x, y, 0, startX, startY, width, height, 256, 256);
	}

	@Override
	public void render(PoseStack ms, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(ms, c, x, y, startX, startY, width, height);
	}

	@Override
	public ResourceLocation getLocation() {
		return location;
	}

	@Override
	public int getStartX() {
		return startX;
	}

	@Override
	public int getStartY() {
		return startY;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
