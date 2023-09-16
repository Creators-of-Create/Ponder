package net.createmod.ponder.api.scene;

import net.minecraft.core.BlockPos;

public interface PositionUtil {
	BlockPos at(int x, int y, int z);

	BlockPos zero();
}
