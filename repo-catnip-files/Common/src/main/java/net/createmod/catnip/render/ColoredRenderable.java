package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.theme.Color;

public interface ColoredRenderable {

	void render(PoseStack ms, int x, int y, Color c);

}
