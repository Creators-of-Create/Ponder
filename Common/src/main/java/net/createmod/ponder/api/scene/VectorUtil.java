package net.createmod.ponder.api.scene;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public interface VectorUtil {
	Vec3 centerOf(int x, int y, int z);

	Vec3 centerOf(BlockPos pos);

	Vec3 topOf(int x, int y, int z);

	Vec3 topOf(BlockPos pos);

	Vec3 blockSurface(BlockPos pos, Direction face);

	Vec3 blockSurface(BlockPos pos, Direction face, float margin);

	Vec3 of(double x, double y, double z);
}
