package net.createmod.ponder.impl.client.tooltip;

import net.createmod.ponder.api.client.HoveredItemProvider;
import net.createmod.ponder.impl.client.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

enum ContainerScreenHoveredItemProvider implements HoveredItemProvider {
	INSTANCE;

	@Override
	public Item determineHoveredItem() {
		if (!(Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreenAccessor screen))
			return Items.AIR;

		Slot slot = screen.ponder$getHoveredSlot();
		return slot == null ? Items.AIR : slot.getItem().getItem();
	}

	public static void init() {
	    HoveredItemProvider.register(INSTANCE);
	}
}
