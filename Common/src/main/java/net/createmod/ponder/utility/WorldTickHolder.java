package net.createmod.ponder.utility;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.worldWrappers.WrappedClientWorld;
import net.createmod.ponder.foundation.PonderWorld;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.world.level.LevelAccessor;

public class WorldTickHolder {
	public static int getTicks(LevelAccessor world) {
		if (world instanceof WrappedClientWorld)
			return getTicks(((WrappedClientWorld) world).getWrappedWorld());
		return world instanceof PonderWorld ? PonderUI.ponderTicks : AnimationTickHolder.getTicks();
	}

	public static float getRenderTime(LevelAccessor world) {
		return getTicks(world) + getPartialTicks(world);
	}

	public static float getPartialTicks(LevelAccessor world) {
		return world instanceof PonderWorld ? PonderUI.getPartialTicks() : AnimationTickHolder.getPartialTicks();
	}
}
