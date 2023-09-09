package net.createmod.ponder.foundation.content;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.api.registration.SharedTextRegistrationHelper;
import net.minecraft.world.level.ItemLike;

public class BasePonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return Ponder.MOD_ID;
	}

	@Override
	public Stream<Predicate<ItemLike>> indexExclusions() {
		Ponder.LOGGER.info("Index Exclusions called!");

		return PonderPlugin.super.indexExclusions();
	}

	@Override
	public void registerSharedText(SharedTextRegistrationHelper helper) {
		helper.registerSharedText("sneak_and", "Sneak +");
		helper.registerSharedText("ctrl_and", "Ctrl +");
	}
}
