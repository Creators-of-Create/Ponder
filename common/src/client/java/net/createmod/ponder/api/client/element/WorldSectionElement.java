package net.createmod.ponder.api.client.element;

import net.createmod.catnip.api.data.Pair;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface WorldSectionElement extends AnimatedSceneElement {

	void mergeOnto(WorldSectionElement other);

	void set(Selection selection);

	void add(Selection toAdd);

	void erase(Selection toErase);

	void setCenterOfRotation(Vec3 center);

	void stabilizeRotation(Vec3 anchor);

	void selectBlock(BlockPos pos);

	void resetSelectedBlock();

	void queueRedraw();

	boolean isEmpty();

	void setEmpty();

	void setAnimatedRotation(Vec3 eulerAngles, boolean force);

	Vec3 getAnimatedRotation();

	void setAnimatedOffset(Vec3 offset, boolean force);

	Vec3 getAnimatedOffset();

	Pair<Vec3, BlockHitResult> rayTrace(PonderLevel level, Vec3 source, Vec3 target);
}
