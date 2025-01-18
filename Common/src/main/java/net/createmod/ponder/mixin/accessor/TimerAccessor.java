package net.createmod.ponder.mixin.accessor;

import net.minecraft.client.DeltaTracker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DeltaTracker.Timer.class)
public interface TimerAccessor {
	@Accessor("deltaTickResidual")
	float catnip$getDeltaTickResidual();
}
