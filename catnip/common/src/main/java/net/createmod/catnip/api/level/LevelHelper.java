package net.createmod.catnip.api.level;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;

public class LevelHelper {
	public static Identifier getDimensionID(LevelAccessor level) {
		return level.registryAccess()
			.lookupOrThrow(Registries.DIMENSION_TYPE)
			.getKey(level.dimensionType());
	}
}
