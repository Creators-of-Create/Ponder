package net.createmod.ponder.utility;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.levelWrappers.WrappedClientLevel;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.world.level.LevelAccessor;

public class LevelTickHolder {
	public static int getTicks(LevelAccessor level) {
		if (level instanceof WrappedClientLevel)
			return getTicks(((WrappedClientLevel) level).getWrappedLevel());
		return level instanceof PonderLevel ? PonderUI.ponderTicks : AnimationTickHolder.getTicks();
	}

	public static float getRenderTime(LevelAccessor level) {
		return getTicks(level) + getPartialTicks(level);
	}

	public static float getPartialTicks(LevelAccessor level) {
		return level instanceof PonderLevel ? PonderUI.getPartialTicks() : AnimationTickHolder.getPartialTicks();
	}
}
