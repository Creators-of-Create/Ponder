package net.createmod.catnip.api.client.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.FadableScreenElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.config.ConfigHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class BaseConfigScreen extends ConfigScreen {

	private static final Map<String, UnaryOperator<BaseConfigScreen>> DEFAULTS = new HashMap<>();
	public static final FadableScreenElement DISABLED_RENDERER = (graphics, width, height, alpha) ->
		UIRenderHelper.angledGradient(graphics, 90, width / 2, -2, width + 4, height + 4, AbstractSimiWidget.COLOR_DISABLED);

	@Nullable
	ModConfigSpec clientSpec;
	@Nullable
	ModConfigSpec commonSpec;
	@Nullable
	ModConfigSpec serverSpec;
	String clientButtonLabel = "Client Config";
	String commonButtonLabel = "Common Config";
	String serverButtonLabel = "Server Config";
	String modID;

	public BaseConfigScreen(@Nullable Screen parent, String modID) {
		super(parent);
		this.modID = modID;

		if (DEFAULTS.containsKey(modID))
			DEFAULTS.get(modID).apply(this);
		else
			searchForConfigSpecs();
	}

	public static void setDefaultActionFor(String modID, UnaryOperator<BaseConfigScreen> transform) {
		DEFAULTS.put(modID, transform);
	}

	public BaseConfigScreen searchForConfigSpecs() {
		if (!ConfigHelper.hasAnyForgeConfig(modID))
			return this;

		try {
			clientSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.CLIENT, modID);
		} catch (ClassCastException | NullPointerException e) {
			ConfigHelper.LOGGER.debug("Unable to find ClientConfigSpec for mod: {}", modID);
		}

		try {
			commonSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.COMMON, modID);
		} catch (ClassCastException | NullPointerException e) {
			ConfigHelper.LOGGER.debug("Unable to find CommonConfigSpec for mod: {}", modID);
		}

		try {
			serverSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.SERVER, modID);
		} catch (ClassCastException | NullPointerException e) {
			ConfigHelper.LOGGER.debug("Unable to find ServerConfigSpec for mod: {}", modID);
		}

		return this;
	}

	public BaseConfigScreen withSpecs(@Nullable ModConfigSpec client, @Nullable ModConfigSpec common, @Nullable ModConfigSpec server) {
		clientSpec = client;
		commonSpec = common;
		serverSpec = server;
		return this;
	}

	public BaseConfigScreen withButtonLabels(@Nullable String client, @Nullable String common, @Nullable String server) {
		if (client != null)
			clientButtonLabel = client;
		if (common != null)
			commonButtonLabel = common;
		if (server != null)
			serverButtonLabel = server;
		return this;
	}

	@Override
	protected void init() {
		super.init();
		int x = width / 2 - 100;
		int y = height / 2 - 36;
		addRenderableWidget(configButton(x, y, clientButtonLabel, clientSpec));
		addRenderableWidget(configButton(x, y + 24, commonButtonLabel, commonSpec));
		addRenderableWidget(configButton(x, y + 48, serverButtonLabel, serverSpec));
	}

	private Button configButton(int x, int y, String label, @Nullable ModConfigSpec spec) {
		Button button = Button.builder(Component.literal(label), $ -> {
		}).bounds(x, y, 200, 20).build();
		button.active = spec != null;
		return button;
	}
}
