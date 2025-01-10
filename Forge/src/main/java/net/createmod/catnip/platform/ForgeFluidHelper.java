package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public class ForgeFluidHelper implements ModFluidHelper<FluidStack> {
	@Override
	public int getColor(Fluid fluid) {
		return IClientFluidTypeExtensions.of(fluid).getTintColor();
	}

	@Override
	public int getColor(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		return IClientFluidTypeExtensions.of(fluid).getTintColor(toStack(fluid, amount, fluidData));
	}

	@Override
	public int getLuminosity(Fluid fluid) {
		return fluid.getFluidType().getLightLevel();
	}


	@Override
	public int getLuminosity(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		return fluid.getFluidType().getLightLevel(toStack(fluid, amount, fluidData));
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid) {
		return IClientFluidTypeExtensions.of(fluid).getStillTexture();
	}


	@Override
	public ResourceLocation getStillTexture(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		return IClientFluidTypeExtensions.of(fluid).getStillTexture(toStack(fluid, amount, fluidData));
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return fluid.getFluidType().isLighterThanAir();
	}

	@Override
	public FluidStack toStack(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		FluidStack fluidStack = new FluidStack(fluid, (int) amount);
		fluidStack.setTag(fluidData);
		return fluidStack;
	}
}
