package net.createmod.catnip.animation;

import net.minecraft.util.Mth;

public class AnimationFunctions {
	// Approximations of some Web animation functions

	public static float easeOut(float t) {
		return Mth.sin(Mth.HALF_PI * t);
	}

	public static float easeInOut(float t) {
		return (float) Math.pow(Mth.sin(Mth.HALF_PI * t), 2);
	}

	public static float easeIn(float t) {
		return (float) Math.pow(t, 1.7);
	}
}
