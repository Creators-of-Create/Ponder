package net.createmod.ponder.api.scene;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.createmod.catnip.utility.outliner.Outline;
import net.createmod.catnip.utility.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Selection extends Predicate<BlockPos> {

	Selection add(Selection other);

	Selection substract(Selection other);

	Selection copy();

	Vec3 getCenter();

	void forEach(Consumer<BlockPos> callback);

	Outline.OutlineParams makeOutline(Outliner outliner, Object slot);

	default Outline.OutlineParams makeOutline(Outliner outliner) {
		return makeOutline(outliner, this);
	}
}
