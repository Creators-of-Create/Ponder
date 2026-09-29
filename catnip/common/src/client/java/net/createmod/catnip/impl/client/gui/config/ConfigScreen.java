package net.createmod.catnip.impl.client.gui.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList.Entry;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.api.config.access.ConfigAccess;

import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.impl.config.ConfigHelper.FoundElement;
import net.createmod.catnip.impl.config.ConfigHelper.FoundElement.Value;
import net.minecraft.client.gui.components.EditBox;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.createmod.catnip.api.client.gui.ConfirmationScreen;
import net.createmod.catnip.api.client.gui.ConfirmationScreen.Response;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.theme.Color;
import net.createmod.catnip.impl.config.packet.ServerboundSetConfigValuesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class ConfigScreen extends ModConfigScreen {
	public static final Component SEARCH = Component.literal("Search");

	public final ConfigId configId;

	private final ConfigAccess rootConfig;
	private final Map<ConfigPath, Object> changes;
	private final ConfigPath.Builder pathBuilder;
	private ConfigAccess currentConfig;

	private @Nullable ConfigEntryList list;
	private @Nullable EditBox search;
	private boolean mutable;

	ConfigScreen(RootConfigScreen parent, ConfigSide side, ConfigAccess rootConfig) {
		// FIXME: translation
		Component title = Component.literal(side.toString() + " config for " + parent.modId);
		super(title, parent.modId, parent);
		this.configId = new ConfigId(parent.modId, side);
		this.rootConfig = rootConfig;
		this.changes = new HashMap<>();
		this.currentConfig = this.rootConfig;
		this.pathBuilder = ConfigPath.builder();
		this.mutable = true;
	}

	/// Apply the given consumer to each element of the current config.
	/// Note that [values][ConfigAccess.Value] should never be gotten or set directly due to change tracking.
	/// Instead, use [getValue][#getValue(ConfigPath)] and [setValue][#setValue(ConfigPath, Object)].
	public void forEachElement(BiConsumer<ConfigAccess.Element, ConfigPath> consumer) {
		for (Iterator<? extends ConfigAccess.Element> itr = this.currentConfig.elements(); itr.hasNext();) {
			ConfigAccess.Element element = itr.next();
			ConfigPath path = this.pathBuilder.buildWith(element.name());
			consumer.accept(element, path);
		}
	}

	/// Try to get the value at the given path.
	///
	/// May fail if the path points to a nested config or nothing at all.
	public Optional<Object> getValue(ConfigPath path) {
		return this.findValue(path).map(value -> {
			Object changed = this.changes.get(path);
			return changed != null ? changed : value.get();
		});
	}

	/// Try to set the value at the given path.
	///
	/// May fail if the path points to a nested config or nothing at all.
	/// @return true if a value was set
	public boolean setValue(ConfigPath path, Object value) {
		Objects.requireNonNull(value, "value");
		Optional<ConfigAccess.Value<?>> found = this.findValue(path);

		if (found.isPresent()) {
			if (value.equals(found.get())) {
				this.changes.remove(path);
			} else {
				this.changes.put(path, value);
			}

			return true;
		} else {
			return false;
		}
	}

	/// @return true if a change is queued for the value at the given path
	public boolean isDirty(ConfigPath path) {
		return this.changes.containsKey(path);
	}

	/// @return true if the player has permission to modify this config
	public boolean isMutable() {
		return this.mutable;
	}

	/// Navigate to the given path.
	///
	/// If the path leads to a value, it will be highlighted.
	/// If the path leads to a nested config, it will be opened.
	///
	/// Navigation may fail if any component of the given path does not correspond to an element.
	/// @return true if navigation was successful
	public boolean navigate(ConfigPath path) {
		return switch (ConfigHelper.findElement(this.rootConfig, path)) {
			case FoundElement.None _ -> false;

			case FoundElement.Nested(ConfigAccess.Nested nested) -> {
				this.switchToConfig(nested);
				yield true;
			}

			case FoundElement.Value(ConfigAccess config, ConfigAccess.Value<?> value) -> {
				this.switchToConfig(config);

				for (ConfigEntryList.Entry entry : Objects.requireNonNull(this.list).children()) {
					if (entry.name().equals(value.name())) {
						entry.highlight();
						break;
					}
				}

				yield true;
			}
		};
	}

	private Optional<ConfigAccess.Value<?>> findValue(ConfigPath path) {
		return ConfigHelper.findValue(this.rootConfig, path);
	}

	private void switchToConfig(ConfigAccess config) {
		if (config == this.currentConfig)
			return;

		String currentSearch = this.search == null ? "" : this.search.getValue();
		this.currentConfig = config;
		this.rebuildWidgets();
		Objects.requireNonNull(this.search).setValue(currentSearch);
	}

	protected void clearChanges() {
		this.changes.clear();

		if (this.list != null) {
			for (ConfigEntryList.Entry e : this.list.children()) {
				if (e instanceof ValueEntry<?> valueEntry) {
					valueEntry.notifyOfValueChange();
				}
			}
		}
	}

	/// Applies all current changes.
	protected void saveChanges() {
		this.changes.forEach((path, newValue) -> {
			ConfigAccess.Value<?> value = this.findValue(path).orElseThrow();
			setValueUnchecked(value, newValue);

			if (this.configId.side() == ConfigSide.SERVER) {
				ClientNetworkHelper.INSTANCE.sendToServer(ServerboundSetConfigValuesPacket.wrap(this.configId.modId(), this.changes));
			}
		});

		this.clearChanges();
	}

	/// Resets the config, queueing the required changes.
	private void resetConfig() {
		this.resetConfig(this.rootConfig, ConfigPath.builder());

		if (this.list != null) {
			for (ConfigEntryList.Entry e : this.list.children()) {
				if (e instanceof ValueEntry<?> valueEntry) {
					valueEntry.notifyOfValueChange();
				}
			}
		}
	}

	private void resetConfig(ConfigAccess config, ConfigPath.Builder pathBuilder) {
		for (Iterator<? extends ConfigAccess.Element> itr = config.elements(); itr.hasNext();) {
			switch (itr.next()) {
				case ConfigAccess.Nested nested -> {
					pathBuilder.push(nested.name());
					this.resetConfig(nested, pathBuilder);
					pathBuilder.pop();
				}
				case ConfigAccess.Value<?> value -> {
					ConfigPath path = pathBuilder.buildWith(value.name());
					this.changes.put(path, value.getDefault());
				}
			}
		}
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);

		int yCenter = height / 2;
		int listL = this.width / 2 - listWidth / 2;
		int listR = this.width / 2 + listWidth / 2;

		BoxWidget resetAll = new BoxWidget(listR + 10, yCenter - 25, 20, 20)
			.withPadding(2, 2)
			.withCallback((_, _) ->
				new ConfirmationScreen()
					.centered()
					.withText(Component.translatableEscape("catnip.ui.resetting_changes_message", this.configId.side()))
					.withAction(success -> {
						if (success)
							this.resetConfig();
					})
					.open(this)
			);

		resetAll.showingElement(CatnipGuiTextures.ICON_CONFIG_RESET.asStencil().withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(resetAll)));
		resetAll.getToolTip().add(Component.translatable("catnip.ui.reset_all_button"));
		resetAll.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.reset_all_button_tooltip"), Palette.ALL_GRAY));

		BoxWidget saveChanges = new BoxWidget(listL - 30, yCenter - 25, 20, 20)
			.withPadding(2, 2)
			.withCallback((_, _) -> {
				if (this.changes.isEmpty())
					return;

				ConfirmationScreen confirm = new ConfirmationScreen()
					.centered()
					.withText(Component.translatable("catnip.ui.saving_changes_message", this.changes.size(), Component.translatable(this.changes.size() != 1 ? "catnip.ui.changed_values_plural" : "catnip.ui.changed_values_singular")))
					.withAction(success -> {
						if (success)
							saveChanges();
					});

				addAttributesToConfirmation(confirm).open(this);
			});
		saveChanges.showingElement(CatnipGuiTextures.ICON_CONFIG_SAVE.asStencil().withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(saveChanges)));
		saveChanges.getToolTip().add(Component.translatable("catnip.ui.save_changes_button"));
		saveChanges.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.save_changes_button_tooltip"), Palette.ALL_GRAY));

		BoxWidget discardChanges = new BoxWidget(listL - 30, yCenter + 5, 20, 20)
			.withPadding(2, 2)
			.withCallback((_, _) -> {
				if (this.changes.isEmpty())
					return;

				new ConfirmationScreen()
					.centered()
					.withText(Component.translatable("catnip.ui.discarding_changes_message", this.changes.size(), Component.translatable(this.changes.size() != 1 ? "catnip.ui.value_changes_plural" : "catnip.ui.value_changes_singular")))
					.withAction(success -> {
						if (success)
							clearChanges();
					})
					.open(this);
			});
		discardChanges.showingElement(CatnipGuiTextures.ICON_CONFIG_DISCARD.asStencil().withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(discardChanges)));
		discardChanges.getToolTip().add(Component.translatable("catnip.ui.discard_changes_button"));
		discardChanges.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.discard_changes_button_tooltip"), Palette.ALL_GRAY));

		BoxWidget goBack = new BoxWidget(listL - 30, yCenter + 65, 20, 20)
			.withPadding(2, 2)
			.withCallback(this::attemptBackstep);
		goBack.showingElement(CatnipGuiTextures.ICON_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(goBack)));
		goBack.getToolTip().add(Component.translatable("catnip.ui.go_back_button"));

		addRenderableWidget(resetAll);
		addRenderableWidget(saveChanges);
		addRenderableWidget(discardChanges);
		addRenderableWidget(goBack);

		search = new EditBox(font, width / 2 - listWidth / 2, height - 35, listWidth, 20, SEARCH);
		search.setResponder(query -> {
			if (this.list != null) {
				this.list.updateFilter(query.toLowerCase(Locale.ROOT));
			}
		});
		search.setHint(Component.translatable("catnip.ui.search_hint"));
		search.moveCursorToStart(false);
		addRenderableWidget(search);

		list = new ConfigEntryList(this, listWidth, height - 80, 35, 40, search.getValue());
		list.setX(this.width / 2 - list.getWidth() / 2);
		addRenderableWidget(list);

		//extras for server configs
		if (this.configId.side() != ConfigSide.SERVER || this.minecraft.hasSingleplayerServer())
			return;

		this.mutable = minecraft.player != null && ConfigHelper.CAN_CHANGE_CONFIGS.check(minecraft.player.permissions());

		Couple<Color> red = AbstractSimiWidget.COLOR_FAIL;
		Couple<Color> green = AbstractSimiWidget.COLOR_SUCCESS;

		DelegatedStencilElement stencil = new DelegatedStencilElement();

		BoxWidget serverLocked = new BoxWidget(listR + 10, yCenter + 5, 20, 20)
			.withPadding(2, 2)
			.showingElement(stencil);

		if (this.mutable) {
			stencil.withStencilRenderer((ms, _, _, _) -> CatnipGuiTextures.ICON_CONFIG_UNLOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, _, _, _) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, green));
			serverLocked.withBorderColors(green);
			serverLocked.getToolTip().add(Component.translatable("catnip.ui.server_config_unlocked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.server_config_unlocked_tooltip"), Palette.ALL_GRAY));
		} else {
			resetAll.active = false;
			stencil.withStencilRenderer((ms, _, _, _) -> CatnipGuiTextures.ICON_CONFIG_LOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, _, _, _) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, red));
			serverLocked.withBorderColors(red);
			serverLocked.getToolTip().add(Component.translatable("catnip.ui.server_config_locked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.server_config_locked_tooltip"), Palette.ALL_GRAY));
		}

		addRenderableWidget(serverLocked);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		int x = width / 2;
		graphics.centeredText(minecraft.font, this.createBreadcrumbs(), x, 15, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
	}

	private String createBreadcrumbs() {
		StringBuilder builder = new StringBuilder(this.modId);
		builder.append(" > ").append(this.configId.side());
		this.pathBuilder.forEachComponent(component -> builder.append(" > ").append(component));
		return builder.toString();
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (super.keyPressed(keyEvent))
			return true;

		if (search != null && keyEvent.hasControlDown()) {
			if (keyEvent.key() == InputConstants.KEY_F) {
				this.setFocused(this.search);
			}
		}

		if (keyEvent.key() == InputConstants.KEY_BACKSPACE) {
			attemptBackstep();
		}

		return false;
	}

	private void attemptBackstep() {
		if (this.changes.isEmpty() || !(parent instanceof RootConfigScreen)) {
			ScreenOpener.open(parent);
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			this.changes.clear();
			ScreenOpener.open(parent);
		});
	}

	@Override
	public void onClose() {
		if (this.changes.isEmpty()) {
			super.onClose();
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			this.changes.clear();
			super.onClose();
		});
	}

	public void showLeavingPrompt(Consumer<Response> action) {
		ConfirmationScreen screen = new ConfirmationScreen()
			.centered()
			.withThreeActions(action)
			.addText(Component.translatable("catnip.ui.leaving_with_changes_message", this.changes.size(), Component.translatable(this.changes.size() != 1 ? "catnip.ui.value_changes_plural" : "catnip.ui.value_changes_singular")));

		addAttributesToConfirmation(screen).open(this);
	}

	private ConfirmationScreen addAttributesToConfirmation(ConfirmationScreen screen) {
		AtomicBoolean relog = new AtomicBoolean(false);
		AtomicBoolean restart = new AtomicBoolean(false);
		this.changes.keySet().forEach(path -> {
			ConfigAttributes attributes = this.findValue(path).orElseThrow().attributes();
			if (attributes.has(ConfigAttribute.Flag.REQUIRES_RELOG)) {
				relog.set(true);
			}

			if (attributes.has(ConfigAttribute.Flag.REQUIRES_RESTART)) {
				restart.set(true);
			}
		});

		if (relog.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(Component.translatable("catnip.ui.relog_required_message"));
		}

		if (restart.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(Component.translatable("catnip.ui.restart_required_message"));
		}

		return screen;
	}

	public static Optional<ConfigScreen> find(ConfigValueId id) {
		return find(id.config()).map(screen -> {
			screen.navigate(id.path());
			return screen;
		});
	}

	public static Optional<ConfigScreen> find(ConfigId id) {
		Optional<RootConfigScreen> root = RootConfigScreen.forMod(id.modId(), null);
		if (root.isEmpty()) {
			return Optional.empty();
		}

		return root.get().createScreenFor(id.side());
	}

	@SuppressWarnings("unchecked")
	private static void setValueUnchecked(ConfigAccess.Value<?> value, Object newValue) {
		((ConfigAccess.Value<Object>) value).set(newValue);
	}
}
