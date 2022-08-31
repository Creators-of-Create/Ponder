package net.createmod.ponder.foundation;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.level.ItemLike;

public interface PonderPlugin {

	/**
	 * Register all Ponder Scenes added by Mod
	 */
	default void registerScenes() {}

	default void registerTags() {}

	default void onPonderWorldRestore(PonderWorld world) {}

	default Stream<Predicate<ItemLike>> indexExclusions() {
		return Stream.empty();
	}

}
