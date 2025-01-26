package net.createmod.catnip.config.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.createmod.ponder.Ponder;

import net.createmod.ponder.enums.PonderGuiTextures;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import org.lwjgl.glfw.GLFW;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.FadableScreenElement;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class BaseConfigScreen extends ConfigScreen {

	public static final Color COLOR_TITLE_A = new Color(0xff_c69fbc).setImmutable();
	public static final Color COLOR_TITLE_B = new Color(0xff_f6b8bb).setImmutable();
	public static final Color COLOR_TITLE_C = new Color(0xff_fbf994).setImmutable();

	public static final FadableScreenElement DISABLED_RENDERER = (ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_DISABLED);
	private static final Map<String, UnaryOperator<BaseConfigScreen>> DEFAULTS = new HashMap<>();

	/**
	 * If you want to change the config labels, add a default action here.
	 * Make sure you call either {@link #withSpecs(ModConfigSpec, ModConfigSpec, ModConfigSpec)}
	 * or {@link #searchForConfigSpecs()}
	 *
	 * @param modID     the modID of your addon/mod
	 */
	public static void setDefaultActionFor(String modID, UnaryOperator<BaseConfigScreen> transform) {
		DEFAULTS.put(modID, transform);
	}

	@Nullable BoxWidget clientConfigWidget;
	@Nullable BoxWidget commonConfigWidget;
	@Nullable BoxWidget serverConfigWidget;
	@Nullable BoxWidget goBack;
	@Nullable BoxWidget others;
	@Nullable BoxWidget title;

	@Nullable ModConfigSpec clientSpec;
	@Nullable ModConfigSpec commonSpec;
	@Nullable ModConfigSpec serverSpec;
	String clientButtonLabel = "Client Config";
	String commonButtonLabel = "Common Config";
	String serverButtonLabel = "Server Config";
	String modID;
	protected boolean returnOnClose;

	public BaseConfigScreen(@Nullable Screen parent, String modID) {
		super(parent);
		this.modID = modID;

		if (DEFAULTS.containsKey(modID))
			DEFAULTS.get(modID).apply(this);
		else {
			this.searchForConfigSpecs();
		}
	}

	/**
	 * If you have static references to your Configs or ConfigSpecs (like Create does in AllConfigs),
	 * please use {@link #withSpecs(ModConfigSpec, ModConfigSpec, ModConfigSpec)} instead
	 */
	public BaseConfigScreen searchForConfigSpecs() {
		if (!ConfigHelper.hasAnyForgeConfig(this.modID)){
			return this;
		}

		try {
			clientSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.CLIENT, this.modID);
		} catch (ClassCastException | NullPointerException e) {
			Ponder.LOGGER.debug("Unable to find ClientConfigSpec for mod: " + this.modID);
		}

		try {
			commonSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.COMMON, this.modID);
		} catch (ClassCastException | NullPointerException e) {
			Ponder.LOGGER.debug("Unable to find CommonConfigSpec for mod: " + this.modID);
		}

		try {
			serverSpec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.SERVER, this.modID);
		} catch (ClassCastException | NullPointerException e) {
			Ponder.LOGGER.debug("Unable to find ServerConfigSpec for mod: " + this.modID);
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
		returnOnClose = true;

		TextStencilElement clientText = new TextStencilElement(font, Component.literal(clientButtonLabel)).centered(true, true);
		addRenderableWidget(clientConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 - 30, 200, 16).showingElement(clientText));

		if (clientSpec != null) {
			clientConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.CLIENT, clientSpec)));
			clientText.withElementRenderer(BoxWidget.gradientFactory.apply(clientConfigWidget));
		} else {
			clientConfigWidget.active = false;
			clientConfigWidget.updateGradientFromState();
			clientText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement commonText = new TextStencilElement(font, Component.literal(commonButtonLabel)).centered(true, true);
		addRenderableWidget(commonConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15, 200, 16).showingElement(commonText));

		if (commonSpec != null) {
			commonConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.COMMON, commonSpec)));
			commonText.withElementRenderer(BoxWidget.gradientFactory.apply(commonConfigWidget));
		} else {
			commonConfigWidget.active = false;
			commonConfigWidget.updateGradientFromState();
			commonText.withElementRenderer(DISABLED_RENDERER);
		}

		TextStencilElement serverText = new TextStencilElement(font, Component.literal(serverButtonLabel)).centered(true, true);
		addRenderableWidget(serverConfigWidget = new BoxWidget(width / 2 - 100, height / 2 - 15 + 30, 200, 16).showingElement(serverText));

		if (serverSpec == null) {
			serverConfigWidget.active = false;
			serverConfigWidget.updateGradientFromState();
			serverText.withElementRenderer(DISABLED_RENDERER);
		} else if (minecraft.level == null) {
			serverText.withElementRenderer(DISABLED_RENDERER);
			serverConfigWidget.getToolTip()
					.add(Component.literal("Stored individually per World"));
			serverConfigWidget.getToolTip()
					.addAll(FontHelper.cutTextComponent(
						Component.literal("Gameplay settings can only be accessed from the in-game menu after joining a World or Server."),
							Palette.ALL_GRAY));
		} else {
			serverConfigWidget.withCallback(() -> linkTo(new SubMenuConfigScreen(this, ModConfig.Type.SERVER, serverSpec)));
			serverText.withElementRenderer(BoxWidget.gradientFactory.apply(serverConfigWidget));
		}

		TextStencilElement titleText = new TextStencilElement(font, getModDisplayName(modID).orElse(modID.toLowerCase(
				Locale.ROOT)))
				.centered(true, true)
				.withElementRenderer((ms, w, h, alpha) -> {
					UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w / 2, COLOR_TITLE_A, COLOR_TITLE_B);
					UIRenderHelper.angledGradient(ms, 0, w / 2, h / 2, h, w / 2, COLOR_TITLE_B, COLOR_TITLE_C);
				});
		int boxWidth = width + 10;
		int boxHeight = 39;
		int boxPadding = 4;
		title = new BoxWidget(-5, height / 2 - 110, boxWidth, boxHeight)
				//.withCustomBackground(new Color(0x20_000000, true))
				.<BoxWidget>setActive(false)
				.withBorderColors(AbstractSimiWidget.COLOR_IDLE)
				.withPadding(0, boxPadding)
				.rescaleElement(boxWidth / 2f, (boxHeight - 2 * boxPadding) / 2f)//double the text size by telling it the element is only half as big as the available space
				.showingElement(titleText.at(0, 7));

		addRenderableWidget(title);


		ConfigScreen.modID = this.modID;

		goBack = new BoxWidget(width / 2 - 134, height / 2, 20, 20).withPadding(2, 2)
				.withCallback(() -> linkTo(parent));
		goBack.showingElement(PonderGuiTextures.ICON_CONFIG_BACK.asStencil()
				.withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip()
				.add(Component.literal("Go Back"));
		addRenderableWidget(goBack);

		TextStencilElement othersText = new TextStencilElement(font, Component.literal("Access Configs of other Mods")).centered(true, true);
		others = new BoxWidget(width / 2 - 100, height / 2 - 15 + 90, 200, 16).showingElement(othersText);
		othersText.withElementRenderer(BoxWidget.gradientFactory.apply(others));
		others.withCallback(() -> linkTo(new ConfigModListScreen(this)));
		addRenderableWidget(others);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.drawCenteredString(font, "Access Configs for Mod:", width / 2, height / 2 - 105, UIRenderHelper.COLOR_TEXT_STRONG_ACCENT.getFirst().getRGB());
	}

	private void linkTo(@Nullable Screen screen) {
		returnOnClose = false;
		ScreenOpener.open(screen);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			linkTo(parent);
		}
		return false;
	}

}
