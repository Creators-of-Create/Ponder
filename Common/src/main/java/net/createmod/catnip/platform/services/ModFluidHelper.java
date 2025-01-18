package net.createmod.catnip.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public interface ModFluidHelper<R> {
	int getColor(Fluid fluid);

	default int getColor(Fluid fluid, long amount) {
		return getColor(fluid, amount, null);
	}

	int getColor(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	int getLuminosity(Fluid fluid);

	default int getLuminosity(Fluid fluid, long amount) {
		return getLuminosity(fluid, amount, null);
	}

	int getLuminosity(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	ResourceLocation getStillTexture(Fluid fluid);

	default ResourceLocation getStillTexture(Fluid fluid, long amount) {
		return getStillTexture(fluid, amount, null);
	}

	ResourceLocation getStillTexture(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	boolean isLighterThanAir(Fluid fluid);

	default R toStack(Fluid fluid, long amount) {
		return toStack(fluid, amount, null);
	}

	R toStack(Fluid fluid, long amount, @Nullable CompoundTag fluidData);
}
