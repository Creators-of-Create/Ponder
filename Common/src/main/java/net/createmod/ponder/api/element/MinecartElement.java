package net.createmod.ponder.api.element;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface MinecartElement extends AnimatedSceneElement {
	void setPositionOffset(Vec3 position, boolean immediate);

	void setRotation(float angle, boolean immediate);

	Vec3 getPositionOffset();

	Vec3 getRotation();

	interface MinecartConstructor {
		AbstractMinecart create(Level w, double x, double y, double z);
	}
}
