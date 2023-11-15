package net.createmod.ponder.api.scene;

import java.util.function.Supplier;

import net.createmod.ponder.api.element.AnimatedSceneElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.MinecartElement;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public interface SpecialInstructions {
	ElementLink<ParrotElement> createBirb(Vec3 location, Supplier<? extends ParrotPose> pose);

	void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose);

	void movePointOfInterest(Vec3 location);

	void movePointOfInterest(BlockPos location);

	void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation,
					  int duration);

	void moveParrot(ElementLink<ParrotElement> link, Vec3 offset, int duration);

	ElementLink<MinecartElement> createCart(Vec3 location, float angle, MinecartElement.MinecartConstructor type);

	void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration);

	void moveCart(ElementLink<MinecartElement> link, Vec3 offset, int duration);

	<T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, Direction direction);
}
