package net.createmod.ponder.api.scene;

import net.createmod.ponder.foundation.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public interface SelectionUtil {
	Selection everywhere();

	Selection position(int x, int y, int z);

	Selection position(BlockPos pos);

	Selection fromTo(int x, int y, int z, int x2, int y2, int z2);

	Selection fromTo(BlockPos pos1, BlockPos pos2);

	Selection column(int x, int z);

	Selection layer(int y);

	Selection layersFrom(int y);

	Selection layers(int y, int height);

	Selection cuboid(BlockPos origin, Vec3i size);
}
