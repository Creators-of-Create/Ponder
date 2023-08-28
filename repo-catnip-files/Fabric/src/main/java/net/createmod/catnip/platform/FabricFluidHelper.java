package net.createmod.catnip.platform;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FabricFluidHelper implements ModFluidHelper<FluidStack> {
	@Override
	public int getColor(Fluid fluid) {
		return FluidRenderHandlerRegistry.INSTANCE.get(fluid).getFluidColor(null, null, fluid.defaultFluidState());
	}

	@Override
	public int getColor(Fluid fluid, long amount) {
		return getLuminosity(fluid);
	}

	@Override
	public int getLuminosity(Fluid fluid) {
		return FluidVariantAttributes.getLuminance(FluidVariant.of(fluid));
	}

	@Override
	public int getLuminosity(Fluid fluid, long amount) {
		return getLuminosity(fluid);
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid) {
		return FluidVariantRendering.getSprite(FluidVariant.of(fluid)).getName();
	}

	@Override
	public ResourceLocation getStillTexture(Fluid fluid, long amount) {
		return getStillTexture(fluid);
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid));
	}

	@Override
	public FluidStack toStack(Fluid fluid, long amount) {
		return new FluidStack(fluid, amount);
	}
}
