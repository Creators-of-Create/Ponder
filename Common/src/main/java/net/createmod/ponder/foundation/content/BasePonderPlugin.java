package net.createmod.ponder.foundation.content;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderPlugin;
import net.minecraft.world.level.ItemLike;

public class BasePonderPlugin implements PonderPlugin {

	@Override
	public String getModID() {
		return Ponder.MOD_ID;
	}

	@Override
	public Stream<Predicate<ItemLike>> indexExclusions() {
		Ponder.LOGGER.info("Index Exclusions called!");

		return PonderPlugin.super.indexExclusions();
	}

	@Override
	public void registerSharedText(BiConsumer<String, String> adder) {
		adder.accept("sneak_and", "Sneak +");
		adder.accept("ctrl_and", "Ctrl +");
	}
}
