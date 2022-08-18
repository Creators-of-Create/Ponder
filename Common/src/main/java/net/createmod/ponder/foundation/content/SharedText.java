package net.createmod.ponder.foundation.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.createmod.catnip.utility.Pair;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderLocalization;
import net.minecraft.resources.ResourceLocation;

public class SharedText {

	private static final List<Pair<String, Consumer<BiConsumer<String, String>>>> sharedTextProviders = new ArrayList<>();

	public static void registerText(String modID, Consumer<BiConsumer<String, String>> addConsumer) {
		sharedTextProviders.add(Pair.of(modID, addConsumer));
	}

	static {
		registerText(Ponder.MOD_ID, SharedText::baseSharedText);
	}

	public static void gatherText() {
		// Add entries used across several ponder scenes (Safe for hotswap)
		sharedTextProviders.forEach(pair -> pair
				.getSecond()
				.accept((key, value) -> add(pair.getFirst(), key, value))
		);
	}

	private static void baseSharedText(BiConsumer<String, String> adder) {
		adder.accept("sneak_and", "Sneak +");
		adder.accept("ctrl_and", "Ctrl +");
	}

	public static String get(ResourceLocation key) {
		return PonderLocalization.getShared(key);
	}

	private static void add(String modID, String key, String value) {
		PonderLocalization.registerShared(new ResourceLocation(modID, key), value);
	}
}
