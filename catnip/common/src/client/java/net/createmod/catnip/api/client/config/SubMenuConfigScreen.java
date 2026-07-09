package net.createmod.catnip.api.client.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.config.entries.BooleanEntry;
import net.createmod.catnip.api.client.config.entries.EnumEntry;
import net.createmod.catnip.api.client.config.entries.NumberEntry;
import net.createmod.catnip.api.client.config.entries.StringEntry;
import net.createmod.catnip.api.client.config.entries.SubMenuEntry;
import net.createmod.catnip.api.client.config.entries.ValueEntry;
import net.createmod.catnip.api.client.gui.ConfirmationScreen;
import net.createmod.catnip.api.client.gui.ConfirmationScreen.Response;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.theme.Color;
import net.createmod.catnip.api.client.config.ConfigScreenList.LabeledEntry;
import net.createmod.catnip.impl.network.ServerboundConfigPacket;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SubMenuConfigScreen extends ConfigScreen {

	public final ModConfig.Type type;
	protected ModConfigSpec spec;
	protected UnmodifiableConfig configGroup;
	protected ConfigScreenList list;

	@Nullable
	protected BoxWidget resetAll;
	@Nullable
	protected BoxWidget saveChanges;
	@Nullable
	protected BoxWidget discardChanges;
	@Nullable
	protected BoxWidget goBack;
	@Nullable
	protected BoxWidget serverLocked;
	@Nullable
	protected HintableTextFieldWidget search;
	protected int listWidth;
	protected String title;
	protected Set<String> highlights = new HashSet<>();

	public static SubMenuConfigScreen find(net.createmod.catnip.api.config.ConfigHelper.ConfigPath path) {
		ModConfigSpec spec = net.createmod.catnip.api.config.ConfigHelper.findModConfigSpecFor(path.getType(), path.getModID());
		UnmodifiableConfig values = spec.getValues();
		net.createmod.catnip.api.client.config.BaseConfigScreen base = new BaseConfigScreen(null, path.getModID());
		SubMenuConfigScreen screen = new SubMenuConfigScreen(base, "root", path.getType(), spec, values);
		List<String> remainingPath = Lists.newArrayList(path.getPath());

		path:
		while (!remainingPath.isEmpty()) {
			String next = remainingPath.remove(0);
			for (Map.Entry<String, Object> entry : values.valueMap().entrySet()) {
				String key = entry.getKey();
				Object obj = entry.getValue();
				if (!key.equalsIgnoreCase(next))
					continue;

				if (!(obj instanceof AbstractConfig)) {
					//highlight entry
					screen.highlights.add(path.getPath()[path.getPath().length - 1]);
					continue;
				}

				values = (UnmodifiableConfig) obj;
				screen = new SubMenuConfigScreen(screen, toHumanReadable(key), path.getType(), spec, values);
				continue path;
			}

			break;
		}

		ConfigScreen.modID = path.getModID();
		return screen;
	}

	public SubMenuConfigScreen(@Nullable Screen parent, String title, ModConfig.Type type, ModConfigSpec configSpec, UnmodifiableConfig configGroup) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = title;
		this.configGroup = configGroup;
	}

	public SubMenuConfigScreen(Screen parent, ModConfig.Type type, ModConfigSpec configSpec) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = "root";
		this.configGroup = configSpec.getValues();
	}

	protected void clearChanges() {
		net.createmod.catnip.api.config.ConfigHelper.changes.clear();
		for (ConfigScreenList.Entry e : list.children()) {
			if (e instanceof ValueEntry<?> valueEntry) {
				valueEntry.onValueChange();
			}
		}
	}

	protected void saveChanges() {
		UnmodifiableConfig values = spec.getValues();
		net.createmod.catnip.api.config.ConfigHelper.changes.forEach((path, change) -> {
			ModConfigSpec.ConfigValue<Object> configValue = values.get(path);
			configValue.set(change.value);
			configValue.save();

			if (type == ModConfig.Type.SERVER) {
				assert ConfigScreen.modID != null;
				ClientNetworkHelper.INSTANCE.sendToServer(new ServerboundConfigPacket<>(ConfigScreen.modID, path, change.value));
			}

			String command = change.annotations.get("Execute");
			if (minecraft.player != null && command != null && command.startsWith("/")) {
				minecraft.player.connection.sendCommand(command.substring(1));
			}
		});
		clearChanges();
	}

	protected void resetConfig(UnmodifiableConfig values) {
		values.valueMap().forEach((key, obj) -> {
			if (obj instanceof AbstractConfig) {
				resetConfig((UnmodifiableConfig) obj);
			} else if (obj instanceof ModConfigSpec.ConfigValue) {
				ModConfigSpec.ConfigValue<Object> configValue = (ModConfigSpec.ConfigValue<Object>) obj;
				ModConfigSpec.ValueSpec valueSpec = spec.getSpec().getRaw(configValue.getPath());

				List<String> comments = new ArrayList<>();

				if (valueSpec.getComment() != null)
					comments.addAll(Arrays.asList(valueSpec.getComment().split("\n")));

				Pair<String, Map<String, String>> metadata = net.createmod.catnip.api.config.ConfigHelper.readMetadataFromComment(comments);

				net.createmod.catnip.api.config.ConfigHelper.setValue(String.join(".", configValue.getPath()), configValue, valueSpec.getDefault(), metadata.getSecond());
			}
		});

		for (ConfigScreenList.Entry e : list.children()) {
			if (e instanceof ValueEntry<?> valueEntry) {
				valueEntry.onValueChange();
			}
		}
	}

	@Override
	protected void init() {
		super.init();

		listWidth = Math.min(width - 80, 300);

		int yCenter = height / 2;
		int listL = this.width / 2 - listWidth / 2;
		int listR = this.width / 2 + listWidth / 2;

		resetAll = new BoxWidget(listR + 10, yCenter - 25, 20, 20)
			.withPadding(2, 2)
			.withCallback((x, y) ->
				new ConfirmationScreen()
					.centered()
					.withText(Component.translatable("catnip.ui.resetting_changes_message", type.toString()))
					.withAction(success -> {
						if (success)
							resetConfig(spec.getValues());
					})
					.open(this)
			);

		resetAll.showingElement(CatnipGuiTextures.ICON_CONFIG_RESET.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(resetAll)));
		resetAll.getToolTip().add(Component.translatable("catnip.ui.reset_all_button"));
		resetAll.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.reset_all_button_tooltip"), Palette.ALL_GRAY));

		saveChanges = new BoxWidget(listL - 30, yCenter - 25, 20, 20)
			.withPadding(2, 2)
			.withCallback((x, y) -> {
				if (net.createmod.catnip.api.config.ConfigHelper.changes.isEmpty())
					return;

				ConfirmationScreen confirm = new ConfirmationScreen()
					.centered()
					.withText(Component.translatable("catnip.ui.saving_changes_message", net.createmod.catnip.api.config.ConfigHelper.changes.size(), Component.translatable(net.createmod.catnip.api.config.ConfigHelper.changes.size() != 1 ? "catnip.ui.changed_values_plural" : "catnip.ui.changed_values_singular")))
					.withAction(success -> {
						if (success)
							saveChanges();
					});

				addAnnotationsToConfirm(confirm).open(this);
			});
		saveChanges.showingElement(CatnipGuiTextures.ICON_CONFIG_SAVE.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(saveChanges)));
		saveChanges.getToolTip().add(Component.translatable("catnip.ui.save_changes_button"));
		saveChanges.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.save_changes_button_tooltip"), Palette.ALL_GRAY));

		discardChanges = new BoxWidget(listL - 30, yCenter + 5, 20, 20)
			.withPadding(2, 2)
			.withCallback((x, y) -> {
				if (net.createmod.catnip.api.config.ConfigHelper.changes.isEmpty())
					return;

				new ConfirmationScreen()
					.centered()
					.withText(Component.translatable("catnip.ui.discarding_changes_message", net.createmod.catnip.api.config.ConfigHelper.changes.size(), Component.translatable(net.createmod.catnip.api.config.ConfigHelper.changes.size() != 1 ? "catnip.ui.value_changes_plural" : "catnip.ui.value_changes_singular")))
					.withAction(success -> {
						if (success)
							clearChanges();
					})
					.open(this);
			});
		discardChanges.showingElement(CatnipGuiTextures.ICON_CONFIG_DISCARD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(discardChanges)));
		discardChanges.getToolTip().add(Component.translatable("catnip.ui.discard_changes_button"));
		discardChanges.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.discard_changes_button_tooltip"), Palette.ALL_GRAY));

		goBack = new BoxWidget(listL - 30, yCenter + 65, 20, 20)
			.withPadding(2, 2)
			.withCallback(this::attemptBackstep);
		goBack.showingElement(CatnipGuiTextures.ICON_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip().add(Component.translatable("catnip.ui.go_back_button"));

		addRenderableWidget(resetAll);
		addRenderableWidget(saveChanges);
		addRenderableWidget(discardChanges);
		addRenderableWidget(goBack);

		list = new ConfigScreenList(minecraft, listWidth, height - 80, 35, 40);
		list.setX(this.width / 2 - list.getWidth() / 2);

		addRenderableWidget(list);

		search = new ConfigTextField(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint(Component.translatable("catnip.ui.search_hint"));
		search.moveCursorToStart(false);
		addRenderableWidget(search);

		configGroup.valueMap().forEach((key, obj) -> {
			String humanKey = toHumanReadable(key);

			if (obj instanceof AbstractConfig) {
				SubMenuEntry entry = new SubMenuEntry(this, humanKey, spec, (UnmodifiableConfig) obj);
				entry.path = key;
				list.addConfigEntry(entry);
				if (configGroup.valueMap()
					.size() == 1)
					ScreenOpener.open(
						new SubMenuConfigScreen(parent, humanKey, type, spec, (UnmodifiableConfig) obj));

			} else if (obj instanceof ModConfigSpec.ConfigValue<?> configValue) {
				ModConfigSpec.ValueSpec valueSpec = spec.getSpec().getRaw(configValue.getPath());
				Object value = configValue.get();
				ConfigScreenList.Entry entry = null;

				if (value instanceof Boolean) {
					entry = new BooleanEntry(humanKey, (ModConfigSpec.ConfigValue<Boolean>) configValue, valueSpec);
				} else if (value instanceof Enum) {
					entry = new EnumEntry(humanKey, (ModConfigSpec.ConfigValue<Enum<?>>) configValue, valueSpec);
				} else if (value instanceof Number) {
					entry = NumberEntry.create(value, humanKey, configValue, valueSpec);
				} else if (value instanceof String) {
					entry = new StringEntry(humanKey, (ModConfigSpec.ConfigValue<String>) configValue, valueSpec);
				}

				if (entry == null)
					entry = new LabeledEntry("Impl missing - " + configValue.get().getClass().getSimpleName() + "  " + humanKey + " : " + value);

				if (highlights.contains(key))
					entry.annotations.put("highlight", ":)");

				list.addConfigEntry(entry);
			}
		});

		list.sortEntries((e, e2) -> {
			int group = (e2 instanceof SubMenuEntry ? 1 : 0) - (e instanceof SubMenuEntry ? 1 : 0);
			if (group == 0 && e instanceof LabeledEntry le && e2 instanceof LabeledEntry le2) {
				return le.label.getComponent()
					.getString()
					.compareTo(le2.label.getComponent().getString());
			}
			return group;
		});

		list.search(highlights.stream().findFirst().orElse(""));

		//extras for server configs
		if (type != ModConfig.Type.SERVER)
			return;
		if (minecraft.hasSingleplayerServer())
			return;

		boolean canEdit = minecraft.player != null && Commands.LEVEL_GAMEMASTERS.check(minecraft.player.permissions());

		Couple<Color> red = AbstractSimiWidget.COLOR_FAIL;
		Couple<Color> green = AbstractSimiWidget.COLOR_SUCCESS;

		DelegatedStencilElement stencil = new DelegatedStencilElement();

		serverLocked = new BoxWidget(listR + 10, yCenter + 5, 20, 20)
			.withPadding(2, 2)
			.showingElement(stencil);

		if (!canEdit) {
			list.children().forEach(e -> e.setEditable(false));
			resetAll.active = false;
			stencil.withStencilRenderer((ms, w, h, alpha) -> CatnipGuiTextures.ICON_CONFIG_LOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, red));
			serverLocked.withBorderColors(red);
			serverLocked.getToolTip().add(Component.translatable("catnip.ui.server_config_locked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.server_config_locked_tooltip"), Palette.ALL_GRAY));
		} else {
			stencil.withStencilRenderer((ms, w, h, alpha) -> CatnipGuiTextures.ICON_CONFIG_UNLOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, green));
			serverLocked.withBorderColors(green);
			serverLocked.getToolTip().add(Component.translatable("catnip.ui.server_config_unlocked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.server_config_unlocked_tooltip"), Palette.ALL_GRAY));
		}

		addRenderableWidget(serverLocked);
	}

	@Override
	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);

		int x = width / 2;
		graphics.centeredText(minecraft.font, ConfigScreen.modID + " > " + type.toString().toLowerCase(Locale.ROOT) + " > " + title, x, 15, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
	}

	@Override
	protected void renderWindowForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void resize(int width, int height) {
		double scroll = list.scrollBarX();
		init(width, height);
		list.setScrollAmount(scroll);
	}

	@Nullable
	@Override
	public GuiEventListener getFocused() {
		if (ConfigScreenList.currentText != null)
			return ConfigScreenList.currentText;

		return super.getFocused();
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (super.keyPressed(keyEvent))
			return true;

		if (search != null && keyEvent.hasControlDown()) {
			if (keyEvent.key() == InputConstants.KEY_F) {
				search.setFocused(true);
			}
		}

		if (keyEvent.key() == InputConstants.KEY_BACKSPACE) {
			attemptBackstep();
		}

		return false;
	}

	private void updateFilter(String search) {
		if (list.search(search)) {
			this.search.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		} else {
			this.search.setTextColor(AbstractSimiWidget.COLOR_SUCCESS.getFirst().getRGB());
		}
	}

	private void attemptBackstep() {
		if (net.createmod.catnip.api.config.ConfigHelper.changes.isEmpty() || !(parent instanceof BaseConfigScreen)) {
			ScreenOpener.open(parent);
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			net.createmod.catnip.api.config.ConfigHelper.changes.clear();
			ScreenOpener.open(parent);
		});
	}

	@Override
	public void onClose() {
		if (net.createmod.catnip.api.config.ConfigHelper.changes.isEmpty()) {
			super.onClose();
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			net.createmod.catnip.api.config.ConfigHelper.changes.clear();
			super.onClose();
		});
	}

	public void showLeavingPrompt(Consumer<Response> action) {
		ConfirmationScreen screen = new ConfirmationScreen()
			.centered()
			.withThreeActions(action)
			.addText(Component.translatable("catnip.ui.leaving_with_changes_message", net.createmod.catnip.api.config.ConfigHelper.changes.size(), Component.translatable(net.createmod.catnip.api.config.ConfigHelper.changes.size() != 1 ? "catnip.ui.value_changes_plural" : "catnip.ui.value_changes_singular")));

		addAnnotationsToConfirm(screen).open(this);
	}

	private ConfirmationScreen addAnnotationsToConfirm(ConfirmationScreen screen) {
		AtomicBoolean relog = new AtomicBoolean(false);
		AtomicBoolean restart = new AtomicBoolean(false);
		net.createmod.catnip.api.config.ConfigHelper.changes.values().forEach(change -> {
			if (change.annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
				relog.set(true);

			if (change.annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
				restart.set(true);
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

}
