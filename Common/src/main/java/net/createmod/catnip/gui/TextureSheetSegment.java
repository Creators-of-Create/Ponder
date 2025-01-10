package net.createmod.catnip.gui;

import net.createmod.catnip.render.BindableTexture;

public interface TextureSheetSegment extends BindableTexture {

	int getStartX();

	int getStartY();

	int getWidth();

	int getHeight();

}
