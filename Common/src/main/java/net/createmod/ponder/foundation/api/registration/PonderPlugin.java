package net.createmod.ponder.foundation.api.registration;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.createmod.ponder.foundation.PonderLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public interface PonderPlugin {

	/**
	 * @return the modID of the mod that added this plugin
	 */
	String getModId();

	/**
	 * Register all the Ponder Scenes added by your Mod
	 */
	default void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {}

	/**
	 * Register all the Ponder Tags added by your Mod
	 */
	default void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {}

	default void registerSharedText(SharedTextRegistrationHelper helper) {}

	default void onPonderWorldRestore(PonderLevel world) {}

	default Stream<Predicate<ItemLike>> indexExclusions() {
		return Stream.empty();
	}

}
