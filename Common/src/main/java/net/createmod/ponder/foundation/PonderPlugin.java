package net.createmod.ponder.foundation;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.level.ItemLike;

public interface PonderPlugin {

	/**
	 * @return the modID of the mod that added this plugin
	 */
	String getModID();

	/**
	 * Register all the Ponder Scenes added by your Mod
	 */
	default void registerScenes() {}

	/**
	 * Register all the Ponder Tags added by your Mod
	 */
	default void registerTags() {}

	/**
	 * @param adder the consumer that accepts the shared text entries. <br />
	 *              first argument is the key and second its english translation
	 */
	default void registerSharedText(BiConsumer<String, String> adder) {}

	default void onPonderWorldRestore(PonderLevel world) {}

	default Stream<Predicate<ItemLike>> indexExclusions() {
		return Stream.empty();
	}

}
