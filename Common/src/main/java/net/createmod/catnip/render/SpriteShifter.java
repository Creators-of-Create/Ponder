package net.createmod.catnip.render;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;

public class SpriteShifter {

	private static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static SpriteShiftEntry get(ResourceLocation originalLocation, ResourceLocation targetLocation) {
		String key = originalLocation + "->" + targetLocation;
		if (ENTRY_CACHE.containsKey(key))
			return ENTRY_CACHE.get(key);

		SpriteShiftEntry entry = new SpriteShiftEntry();
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> entry.set(originalLocation, targetLocation));
		ENTRY_CACHE.put(key, entry);
		return entry;
	}
}
