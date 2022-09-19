package net.createmod.catnip.render;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;

public class SpriteShifter {

	protected static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

	public static SpriteShiftEntry get(ResourceLocation originalLocation, ResourceLocation targetLocation) {
		String key = originalLocation + "->" + targetLocation;
		if (ENTRY_CACHE.containsKey(key))
			return ENTRY_CACHE.get(key);

		SpriteShiftEntry entry = new SpriteShiftEntry(originalLocation, targetLocation);
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

	public static Stream<ResourceLocation> getAllTargetSprites() {
		return ENTRY_CACHE.values().stream().map(SpriteShiftEntry::getTargetResourceLocation);
	}

	public static Stream<SpriteShiftEntry> getAllShifts() {
		return ENTRY_CACHE.values().stream();
	}
}
