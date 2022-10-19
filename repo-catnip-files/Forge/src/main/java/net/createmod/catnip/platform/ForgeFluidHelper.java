package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ForgeFluidHelper implements ModFluidHelper<FluidStack> {
	@Override
	public int getColor(Fluid fluid) {
		return fluid.getAttributes().getColor();
	}

	@Override
	public int getLuminosity(Fluid fluid) {
		return fluid.getAttributes().getLuminosity();
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid) {
		return fluid.getAttributes().getStillTexture();
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return fluid.getAttributes().isLighterThanAir();
	}

	@Override
	public FluidStack toStack(Fluid fluid, long amount) {
		return new FluidStack(fluid, (int) amount);
	}
}
