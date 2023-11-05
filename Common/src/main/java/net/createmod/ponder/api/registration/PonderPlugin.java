package net.createmod.ponder.api.registration;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.resources.ResourceLocation;

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

	default void onPonderLevelRestore(PonderLevel ponderLevel) {}

	default void indexExclusions(IndexExclusionHelper helper) {}

}
