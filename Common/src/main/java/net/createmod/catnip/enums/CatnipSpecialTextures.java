package net.createmod.catnip.enums;

import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.render.BindableTexture;
import net.minecraft.resources.ResourceLocation;

public enum CatnipSpecialTextures implements BindableTexture {

	BLANK("blank.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private final ResourceLocation location;

	CatnipSpecialTextures(String filename) {
		location = Catnip.asResource(ASSET_PATH + filename);
	}

	@Override
	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

	@Override
	public ResourceLocation getLocation() {
		return location;
	}

}
