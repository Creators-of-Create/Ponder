package net.createmod.ponder.api.registration;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

public interface LangRegistryAccess {

	/**
	 * Generate a JsonObject holding all Lang-entries and their enUS default that was declared in code
	 */
	JsonObject provideLangEntries(String modID);

	String getShared(ResourceLocation key);

	String getTagName(ResourceLocation key);

	String getTagDescription(ResourceLocation key);

	String getSpecific(ResourceLocation sceneId, String k);

}
