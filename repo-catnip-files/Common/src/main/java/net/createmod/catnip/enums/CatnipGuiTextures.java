package net.createmod.catnip.enums;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.render.ColoredRenderable;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.resources.ResourceLocation;

public enum CatnipGuiTextures implements TextureSheetSegment, ColoredRenderable {

	// PlacementIndicator
	PLACEMENT_INDICATOR_SHEET("placement_indicator", 0, 0, 16, 256),

	;

	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;

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
