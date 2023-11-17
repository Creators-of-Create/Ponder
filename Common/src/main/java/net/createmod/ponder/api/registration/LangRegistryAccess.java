package net.createmod.ponder.api.registration;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;

public interface LangRegistryAccess {

	/**
	 * Generate all Lang-entries with their enUS defaults that were declared in code and have them consumed by the passed BiConsumer
	 *
	 * @param modId the ModId (or namespace) that you want to collect the lang entries for
	 */
	void provideLang(String modId, BiConsumer<String, String> consumer);

	String getShared(ResourceLocation key);

	String getTagName(ResourceLocation key);

	String getTagDescription(ResourceLocation key);

	String getSpecific(ResourceLocation sceneId, String k);

}
