package net.createmod.catnip.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface BindableTexture {

	@OnlyIn(Dist.CLIENT)
	default void bind() {
		RenderSystem.setShaderTexture(0, getLocation());
	}

	ResourceLocation getLocation();

}
