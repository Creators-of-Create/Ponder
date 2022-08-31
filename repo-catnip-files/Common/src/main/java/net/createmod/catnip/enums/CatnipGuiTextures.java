package net.createmod.catnip.enums;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.render.ColoredRenderable;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public enum CatnipGuiTextures implements TextureSheetSegment, ScreenElement, ColoredRenderable {

	// PlacementIndicator
	PLACEMENT_INDICATOR_SHEET("placement_indicator", 0, 0, 16, 256),

	//icons
	ICON_CONFIG_UNLOCKED("widgets", 0, 0),
	ICON_CONFIG_LOCKED("widgets", 1, 0),
	ICON_CONFIG_DISCARD("widgets", 2, 0),
	ICON_CONFIG_SAVE("widgets", 3, 0),
	ICON_CONFIG_RESET("widgets", 4, 0),
	ICON_CONFIG_BACK("widgets", 5, 0),
	ICON_CONFIG_PREV("widgets", 6, 0),
	ICON_CONFIG_NEXT("widgets", 7, 0),
	ICON_DISABLE("widgets", 8, 0),
	ICON_CONFIG_OPEN("widgets", 9, 0),
	ICON_CONFIRM("widgets", 10, 0),

	;

	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;

	CatnipGuiTextures(String location, int iconColumn, int iconRow) {
		this(location, iconColumn * 16, iconRow * 16, 16, 16);
	}

	CatnipGuiTextures(String location, int startX, int startY, int width, int height) {
		this(Catnip.MOD_ID, location, startX, startY, width, height);
	}

	CatnipGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
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

	public DelegatedStencilElement asStencil() {
		return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.render(ms, 0, 0)).withBounds(16, 16);
	}
}
