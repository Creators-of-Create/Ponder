package net.createmod.catnip.api.client.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import org.apache.logging.log4j.util.TriConsumer;
import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.animation.PhysicalFloat;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ConfigScreen extends AbstractSimiScreen {

	public static final Map<String, TriConsumer<Screen, GuiGraphicsExtractor, Float>> backgrounds = new HashMap<>();
	@Nullable
	public static String modID = null;
	public static final PhysicalFloat cogSpin = PhysicalFloat.create()
		.withDrag(0.4)
		.withLimit(30);

	public static BlockState shadowState = Blocks.POTTED_CRIMSON_ROOTS.defaultBlockState();
	@Nullable
	protected final Screen parent;

	public ConfigScreen(@Nullable Screen parent) {
		super(Component.empty());
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void onClose() {
		if (parent != null && minecraft != null) {
			minecraft.gui.setScreen(parent);
			return;
		}
		super.onClose();
	}

	public static String toHumanReadable(String key) {
		String text = key.replace('_', ' ').replace('-', ' ');
		text = text.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");

		StringBuilder builder = new StringBuilder();
		for (String word : text.split("\\s+")) {
			if (word.isEmpty())
				continue;
			if (!builder.isEmpty())
				builder.append(' ');
			builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
			if (word.length() > 1)
				builder.append(word.substring(1).toLowerCase(Locale.ROOT));
		}
		return builder.toString();
	}
}
