package net.createmod.ponder.impl.client.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Nullable
	@Accessor("hoveredSlot")
	Slot ponder$getHoveredSlot();
}
