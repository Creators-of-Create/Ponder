package net.createmod.ponder.api.element;

import net.minecraft.world.phys.Vec3;

public interface ParrotElement extends AnimatedSceneElement {
	void setPositionOffset(Vec3 position, boolean immediate);

	void setRotation(Vec3 eulers, boolean immediate);

	Vec3 getPositionOffset();

	Vec3 getRotation();

	void setPose(ParrotPose pose);
}
