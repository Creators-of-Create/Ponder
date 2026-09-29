package net.createmod.catnip.impl.client.gui.config;

import net.createmod.catnip.api.client.config.CatnipConfigScreenOptions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

/// A config screen belonging to a mod.
public abstract class ModConfigScreen extends AbstractConfigScreen {
	public final String modId;

	protected ModConfigScreen(Component title, String modId, @Nullable Screen parent) {
		BlockState shadowBlock = CatnipConfigScreenOptions.forMod(modId).shadowBlock();
		super(title, parent, shadowBlock);
		this.modId = modId;
	}
}
