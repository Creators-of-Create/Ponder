package net.createmod.catnip.impl.client.gui.config;

import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.access.ConfigAccess;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.FadableScreenElement;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// The root config screen, before a config has been selected.
/// Provides access to the client, common, and server configs, if present.
public class RootConfigScreen extends ModConfigScreen {
	public static final Color COLOR_TITLE_A = new Color(0xff_c69fbc).setImmutable();
	public static final Color COLOR_TITLE_B = new Color(0xff_f6b8bb).setImmutable();
	public static final Color COLOR_TITLE_C = new Color(0xff_fbf994).setImmutable();

	public static final FadableScreenElement DISABLED_RENDERER = (ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_DISABLED);

	private final Map<ConfigSide, ConfigAccess> configs;
	protected boolean returnOnClose;

	private RootConfigScreen(@Nullable Screen parent, String modId, Map<ConfigSide, ConfigAccess> configs) {
		super(null, modId, parent);
		this.configs = configs;
	}

	@Override
	protected void init() {
		super.init();
		returnOnClose = true;

		for (ConfigSide side : ConfigSide.values()) {
			MutableComponent title = Component.translatable("catnip.ui.%s_config_button_label".formatted(side));
			TextStencilElement text = new TextStencilElement(font, title).centered(true, true);

			int yOffset = switch (side) {
				case CLIENT -> -30;
				case COMMON -> 0;
				case SERVER -> 30;
			};

			BoxWidget widget = new BoxWidget((width / 2) - 100, (height / 2) - 15 + yOffset, 200, 16).showingElement(text);
			this.addRenderableWidget(widget);

			ConfigAccess config = this.configs.get(side);
			if (config != null) {
				if (side != ConfigSide.SERVER || this.minecraft.level != null) {
					// enabled
					widget.withCallback(() -> this.createScreenFor(side).ifPresent(this::linkTo));
					text.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(widget));
				} else {
					// disable the server config button when not in a world
					List<Component> tooltip = widget.getToolTip();
					tooltip.add(Component.translatable("catnip.ui.server_config_unavailable"));
					tooltip.addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.server_config_unavailable_tooltip"), Palette.ALL_GRAY));
				}
			} else {
				// no config, disable
				widget.active = false;
				widget.updateGradientFromState();
				text.withElementRenderer(DISABLED_RENDERER);
			}
		}

		TextStencilElement titleText = new TextStencilElement(font, PlatformHelper.INSTANCE.getModDisplayName(this.modId))
			.centered(true, true)
			.withElementRenderer((ms, w, h, _) -> {
				UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w / 2f, COLOR_TITLE_A, COLOR_TITLE_B);
				UIRenderHelper.angledGradient(ms, 0, w / 2, h / 2, h, w / 2f, COLOR_TITLE_B, COLOR_TITLE_C);
			});
		int boxWidth = width + 10;
		int boxHeight = 39;
		int boxPadding = 4;
		BoxWidget title = new BoxWidget(-5, height / 2 - 110, boxWidth, boxHeight)
			//.withCustomBackground(new Color(0x20_000000, true))
			.<BoxWidget>setActive(false)
			.withBorderColors(AbstractSimiWidget.COLOR_IDLE)
			.withPadding(0, boxPadding)
			.rescaleElement(boxWidth / 2f, (boxHeight - 2 * boxPadding) / 2f)//double the text size by telling it the element is only half as big as the available space
			.showingElement(titleText.at(0, 7));

		addRenderableWidget(title);

		BoxWidget goBack = new BoxWidget(width / 2 - 134, height / 2, 20, 20).withPadding(2, 2)
			.withCallback(() -> linkTo(parent));
		goBack.showingElement(CatnipGuiTextures.ICON_CONFIG_BACK.asStencil()
			.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(goBack)));
		goBack.getToolTip()
			.add(Component.translatable("catnip.ui.go_back_button"));
		addRenderableWidget(goBack);

		TextStencilElement othersText = new TextStencilElement(font, Component.translatable("catnip.ui.other_mods_config_button_label")).centered(true, true);
		BoxWidget others = new BoxWidget(width / 2 - 100, height / 2 - 15 + 90, 200, 16).showingElement(othersText);
		othersText.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(others));
		others.withCallback(() -> linkTo(new ModListConfigScreen(this)));
		addRenderableWidget(others);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(font, Component.translatable("catnip.ui.other_mods_config_title"), width / 2, height / 2 - 105, UIRenderHelper.COLOR_TEXT_STRONG_ACCENT.getFirst().getRGB());
	}

	private void linkTo(@Nullable Screen screen) {
		returnOnClose = false;
		ScreenOpener.open(screen);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (super.keyPressed(keyEvent))
			return true;
		if (keyEvent.key() == InputConstants.KEY_BACKSPACE) {
			linkTo(parent);
		}
		return false;
	}

	public Optional<ConfigScreen> createScreenFor(ConfigSide side) {
		ConfigAccess config = this.configs.get(side);
		return config == null ? Optional.empty() : Optional.of(new ConfigScreen(this, side, config));
	}

	public static Optional<RootConfigScreen> forMod(String id, @Nullable Screen parent) {
		Map<ConfigSide, ConfigAccess> configs = new EnumMap<>(ConfigSide.class);

		for (ConfigSide side : ConfigSide.values()) {
			ConfigId configId = new ConfigId(id, side);
			ConfigAccess config = ConfigAccess.FIND.invoker().get(configId);
			if (config != null) {
				configs.put(side, config);
			}
		}

		if (configs.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new RootConfigScreen(parent, id, configs));
	}
}
