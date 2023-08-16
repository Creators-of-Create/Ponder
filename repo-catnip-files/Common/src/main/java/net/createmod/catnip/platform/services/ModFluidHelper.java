package net.createmod.catnip.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public interface ModFluidHelper<R> {
	int getColor(Fluid fluid);

	int getColor(Fluid fluid, long amount);

	int getLuminosity(Fluid fluid);

	int getLuminosity(Fluid fluid, long amount);

	ResourceLocation getStillTexture(Fluid fluid);

	ResourceLocation getStillTexture(Fluid fluid, long amount);

	boolean isLighterThanAir(Fluid fluid);

	R toStack(Fluid fluid, long amount);
}
