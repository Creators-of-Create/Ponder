package net.createmod.catnip.api.client.config;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/// Per-mod options for Catnip's config screens.
/// @param shadowBlock the BlockState to render in the background. May be air to render nothing.
public record CatnipConfigScreenOptions(BlockState shadowBlock) {
	public static final CatnipConfigScreenOptions NO_BLOCK = new CatnipConfigScreenOptions(Blocks.AIR);
	public static final CatnipConfigScreenOptions DEFAULT = new CatnipConfigScreenOptions(Blocks.CRIMSON_ROOTS);

	private static final Map<String, CatnipConfigScreenOptions> optionsByMod = new HashMap<>();

	static {
		register(Catnip.ID, DEFAULT);
	}

	public CatnipConfigScreenOptions(Block shadowBlock) {
		this(shadowBlock.defaultBlockState());
	}

	/// Register a new set of options to use for config screens belonging to the given mod.
	/// @throws IllegalArgumentException if no mod with the given ID is loaded, or options have already been registered for the given mod
	public static void register(String modId, CatnipConfigScreenOptions options) {
		if (!PlatformHelper.INSTANCE.isModLoaded(modId)) {
			throw new IllegalArgumentException("Mod " + modId + " is not loaded");
		}

		if (optionsByMod.putIfAbsent(modId, options) != null) {
			throw new IllegalArgumentException("Duplicate options registration for mod " + modId);
		}
	}

	/// Get the options to use for the mod with the given ID.
	public static CatnipConfigScreenOptions forMod(String id) {
		return optionsByMod.getOrDefault(id, DEFAULT);
	}
}
