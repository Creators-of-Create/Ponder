package net.createmod.catnip.animation;

import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public class AnimationTickHolder {

	private static int ticks;
	private static int pausedTicks;

	public static void reset() {
		ticks = 0;
		pausedTicks = 0;
	}

	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
		} else {
			pausedTicks = (pausedTicks + 1) % 1_728_000;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + pausedTicks : ticks;
	}

	public static int getTicks(LevelAccessor level) {
		if (level instanceof WrappedClientLevel)
			return getTicks(((WrappedClientLevel) level).getWrappedLevel());
		return level instanceof PonderLevel ? PonderUI.ponderTicks : getTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isPaused() ? mc.pausePartialTick : mc.getFrameTime());
	}

	public static float getPartialTicks(LevelAccessor level) {
		return level instanceof PonderLevel ? PonderUI.getPartialTicks() : getPartialTicks();
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getRenderTime(LevelAccessor level) {
		return getTicks(level) + getPartialTicks(level);
	}
}
