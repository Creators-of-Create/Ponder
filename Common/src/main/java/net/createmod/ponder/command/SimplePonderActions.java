package net.createmod.ponder.command;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderIndexScreen;
import net.createmod.ponder.foundation.ui.PonderTagIndexScreen;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.resources.ResourceLocation;

public class SimplePonderActions {

	public static void openPonder(String value) {
		if (value.equals("index") || value.equals("ponder:index")) {
			ScreenOpener.transitionTo(new PonderIndexScreen());
			return;
		}

		if (value.equals("ponder:tags")) {
			ScreenOpener.transitionTo(new PonderTagIndexScreen());
			return;
		}

		ResourceLocation id = ResourceLocation.parse(value);
		if (!PonderIndex.getSceneAccess().doScenesExistForId(id)) {
			Ponder.LOGGER.error("Could not find ponder scenes for item: " + id);
			return;
		}

		ScreenOpener.transitionTo(PonderUI.of(id));

	}

	public static void reloadPonder(String value) {
		PonderIndex.reload();
	}

}
