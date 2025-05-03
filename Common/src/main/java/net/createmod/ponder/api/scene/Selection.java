package net.createmod.ponder.api.scene;

import java.util.function.Predicate;

import net.createmod.catnip.outliner.Outline;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Selection extends Iterable<BlockPos>, Predicate<BlockPos> {
	Selection add(Selection other);

	Selection substract(Selection other);

	Selection copy();

	Vec3 getCenter();

	Outline.OutlineParams makeOutline(Outliner outliner, Object slot);

	default Outline.OutlineParams makeOutline(Outliner outliner) {
		return makeOutline(outliner, this);
	}
}
