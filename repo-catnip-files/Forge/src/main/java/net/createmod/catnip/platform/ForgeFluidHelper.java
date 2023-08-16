package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class ForgeFluidHelper implements ModFluidHelper<FluidStack> {
	@Override
	public int getColor(Fluid fluid) {
		return IClientFluidTypeExtensions.of(fluid).getTintColor();
	}

	@Override
	public int getColor(Fluid fluid, long amount) {
		return IClientFluidTypeExtensions.of(fluid).getTintColor(toStack(fluid, amount));
	}

	@Override
	public int getLuminosity(Fluid fluid) {
		return fluid.getFluidType().getLightLevel();
	}

	@Override
	public int getLuminosity(Fluid fluid, long amount) {
		return fluid.getFluidType().getLightLevel(toStack(fluid, amount));
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid) {
		return IClientFluidTypeExtensions.of(fluid).getStillTexture();
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid, long amount) {
		return IClientFluidTypeExtensions.of(fluid).getStillTexture(toStack(fluid, amount));
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return fluid.getFluidType().isLighterThanAir();
	}

	@Override
	public FluidStack toStack(Fluid fluid, long amount) {
		return new FluidStack(fluid, (int) amount);
	}
}
