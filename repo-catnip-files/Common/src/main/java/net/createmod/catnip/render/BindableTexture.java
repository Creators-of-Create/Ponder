package net.createmod.catnip.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

public interface BindableTexture {

	default void bind() {
		RenderSystem.setShaderTexture(0, getLocation());
	}

	ResourceLocation getLocation();

}
