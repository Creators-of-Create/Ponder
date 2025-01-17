package net.createmod.catnip.config.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.createmod.ponder.enums.PonderGuiTextures;

import org.lwjgl.glfw.GLFW;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Lists;

import net.createmod.catnip.config.ui.ConfigScreenList.LabeledEntry;
import net.createmod.catnip.config.ui.entries.BooleanEntry;
import net.createmod.catnip.config.ui.entries.EnumEntry;
import net.createmod.catnip.config.ui.entries.NumberEntry;
import net.createmod.catnip.config.ui.entries.SubMenuEntry;
import net.createmod.catnip.config.ui.entries.ValueEntry;
import net.createmod.catnip.gui.ConfirmationScreen;
import net.createmod.catnip.gui.ConfirmationScreen.Response;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.net.ServerboundConfigPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.Components;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class SubMenuConfigScreen extends ConfigScreen {

	public final ModConfig.Type type;
	protected ForgeConfigSpec spec;
	protected UnmodifiableConfig configGroup;
	protected ConfigScreenList list;

	@Nullable protected BoxWidget resetAll;
	@Nullable protected BoxWidget saveChanges;
	@Nullable protected BoxWidget discardChanges;
	@Nullable protected BoxWidget goBack;
	@Nullable protected BoxWidget serverLocked;
	@Nullable protected HintableTextFieldWidget search;
	protected int listWidth;
	protected String title;
	protected Set<String> highlights = new HashSet<>();

	public static SubMenuConfigScreen find(ConfigHelper.ConfigPath path) {
		ForgeConfigSpec spec = ConfigHelper.findForgeConfigSpecFor(path.getType(), path.getModID());
		UnmodifiableConfig values = spec.getValues();
		BaseConfigScreen base = new BaseConfigScreen(null, path.getModID());
		SubMenuConfigScreen screen = new SubMenuConfigScreen(base, "root", path.getType(), spec, values);
		List<String> remainingPath = Lists.newArrayList(path.getPath());

		path: while (!remainingPath.isEmpty()) {
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

	public SubMenuConfigScreen(@Nullable Screen parent, String title, ModConfig.Type type, ForgeConfigSpec configSpec, UnmodifiableConfig configGroup) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = title;
		this.configGroup = configGroup;
	}

	public SubMenuConfigScreen(Screen parent, ModConfig.Type type, ForgeConfigSpec configSpec) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = "root";
		this.configGroup = configSpec.getValues();
	}

	protected void clearChanges() {
		ConfigHelper.changes.clear();
		for (ConfigScreenList.Entry e : list.children()) {
			if (e instanceof ValueEntry<?> valueEntry) {
				valueEntry.onValueChange();
			}
		}
	}

	protected void saveChanges() {
		UnmodifiableConfig values = spec.getValues();
		ConfigHelper.changes.forEach((path, change) -> {
			ForgeConfigSpec.ConfigValue<Object> configValue = values.get(path);
			configValue.set(change.value);

			if (type == ModConfig.Type.SERVER) {
				assert ConfigScreen.modID != null;
				CatnipServices.NETWORK.sendToServer(new ServerboundConfigPacket<>(ConfigScreen.modID, path, change.value));
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
			} else if (obj instanceof ForgeConfigSpec.ConfigValue) {
				ForgeConfigSpec.ConfigValue<Object> configValue = (ForgeConfigSpec.ConfigValue<Object>) obj;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());

				List<String> comments = new ArrayList<>();

				if (valueSpec.getComment() != null)
					comments.addAll(Arrays.asList(valueSpec.getComment().split("\n")));

				Pair<String, Map<String, String>> metadata = ConfigHelper.readMetadataFromComment(comments);

				ConfigHelper.setValue(String.join(".", configValue.getPath()), configValue, valueSpec.getDefault(), metadata.getSecond());
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
								.withText(FormattedText.of("Resetting all settings of the " + type.toString() + " config. Are you sure?"))
								.withAction(success -> {
									if (success)
										resetConfig(spec.getValues());
								})
								.open(this)
				);

		resetAll.showingElement(PonderGuiTextures.ICON_CONFIG_RESET.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(resetAll)));
		resetAll.getToolTip().add(Components.literal("Reset All"));
		resetAll.getToolTip().addAll(FontHelper.cutStringTextComponent("Click here to reset all settings to their default value.", Palette.ALL_GRAY));

		saveChanges = new BoxWidget(listL - 30, yCenter - 25, 20, 20)
				.withPadding(2, 2)
				.withCallback((x, y) -> {
					if (ConfigHelper.changes.isEmpty())
						return;

					ConfirmationScreen confirm = new ConfirmationScreen()
							.centered()
							.withText(FormattedText.of("Saving " + ConfigHelper.changes.size() + " changed value" + (ConfigHelper.changes.size() != 1 ? "s" : "") + ""))
							.withAction(success -> {
								if (success)
									saveChanges();
							});

					addAnnotationsToConfirm(confirm).open(this);
				});
		saveChanges.showingElement(PonderGuiTextures.ICON_CONFIG_SAVE.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(saveChanges)));
		saveChanges.getToolTip().add(Components.literal("Save Changes"));
		saveChanges.getToolTip().addAll(FontHelper.cutStringTextComponent("Click here to save your current changes.", Palette.ALL_GRAY));

		discardChanges = new BoxWidget(listL - 30, yCenter + 5, 20, 20)
				.withPadding(2, 2)
				.withCallback((x, y) -> {
					if (ConfigHelper.changes.isEmpty())
						return;

					new ConfirmationScreen()
							.centered()
							.withText(FormattedText.of("Discarding " + ConfigHelper.changes.size() + " unsaved change" + (ConfigHelper.changes.size() != 1 ? "s" : "") + ""))
							.withAction(success -> {
								if (success)
									clearChanges();
							})
							.open(this);
				});
		discardChanges.showingElement(PonderGuiTextures.ICON_CONFIG_DISCARD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(discardChanges)));
		discardChanges.getToolTip().add(Components.literal("Discard Changes"));
		discardChanges.getToolTip().addAll(FontHelper.cutStringTextComponent("Click here to discard all the changes you made.", Palette.ALL_GRAY));

		goBack = new BoxWidget(listL - 30, yCenter + 65, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::attemptBackstep);
		goBack.showingElement(PonderGuiTextures.ICON_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip().add(Components.literal("Go Back"));

		addRenderableWidget(resetAll);
		addRenderableWidget(saveChanges);
		addRenderableWidget(discardChanges);
		addRenderableWidget(goBack);

		list = new ConfigScreenList(minecraft, listWidth, height - 80, 35, height - 45, 40);
		list.setLeftPos(this.width / 2 - list.getWidth() / 2);

		addRenderableWidget(list);

		search = new ConfigTextField(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint("Ctrl + F to Search...");
		search.moveCursorToStart();
		addRenderableWidget(search);

		configGroup.valueMap().forEach((key, obj) -> {
			String humanKey = toHumanReadable(key);

			if (obj instanceof AbstractConfig) {
				SubMenuEntry entry = new SubMenuEntry(this, humanKey, spec, (UnmodifiableConfig) obj);
				entry.path = key;
				list.children().add(entry);
				if (configGroup.valueMap()
						.size() == 1)
					ScreenOpener.open(
							new SubMenuConfigScreen(parent, humanKey, type, spec, (UnmodifiableConfig) obj));

			} else if (obj instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
				Object value = configValue.get();
				ConfigScreenList.Entry entry = null;

				if (value instanceof Boolean) {
					entry = new BooleanEntry(humanKey, (ForgeConfigSpec.ConfigValue<Boolean>) configValue, valueSpec);
				} else if (value instanceof Enum) {
					entry = new EnumEntry(humanKey, (ForgeConfigSpec.ConfigValue<Enum<?>>) configValue, valueSpec);
				} else if (value instanceof Number) {
					entry = NumberEntry.create(value, humanKey, configValue, valueSpec);
				}

				if (entry == null)
					entry = new LabeledEntry("Impl missing - " + configValue.get().getClass().getSimpleName() + "  " + humanKey + " : " + value);

				if (highlights.contains(key))
					entry.annotations.put("highlight", ":)");

				list.children().add(entry);
			}
		});

		list.children().sort((e, e2) -> {
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

		boolean canEdit = minecraft != null && minecraft.player != null && minecraft.player.hasPermissions(2);

		Couple<Color> red = AbstractSimiWidget.COLOR_FAIL;
		Couple<Color> green = AbstractSimiWidget.COLOR_SUCCESS;

		DelegatedStencilElement stencil = new DelegatedStencilElement();

		serverLocked = new BoxWidget(listR + 10, yCenter + 5, 20, 20)
				.withPadding(2, 2)
				.showingElement(stencil);

		if (!canEdit) {
			list.children().forEach(e -> e.setEditable(false));
			resetAll.active = false;
			stencil.withStencilRenderer((ms, w, h, alpha) -> PonderGuiTextures.ICON_CONFIG_LOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, red));
			serverLocked.withBorderColors(red);
			serverLocked.getToolTip().add(Components.literal("Locked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutStringTextComponent("You do not have enough permissions to edit the server config. You can still look at the current values here though.", Palette.ALL_GRAY));
		} else {
			stencil.withStencilRenderer((ms, w, h, alpha) -> PonderGuiTextures.ICON_CONFIG_UNLOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, green));
			serverLocked.withBorderColors(green);
			serverLocked.getToolTip().add(Components.literal("Unlocked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(FontHelper.cutStringTextComponent("You have enough permissions to edit the server config. Changes you make here will be synced with the server when you save them.", Palette.ALL_GRAY));
		}

		addRenderableWidget(serverLocked);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);

		int x = width / 2;
		graphics.drawCenteredString(minecraft.font, ConfigScreen.modID + " > " + type.toString().toLowerCase(Locale.ROOT) + " > " + title, x, 15, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
	}

	@Override
	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		double scroll = list.getScrollAmount();
		init(client, width, height);
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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;

		if (search != null && Screen.hasControlDown()) {
			if (keyCode == GLFW.GLFW_KEY_F) {
				search.setFocused(true);
			}
		}

		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
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
		if (ConfigHelper.changes.isEmpty() || !(parent instanceof BaseConfigScreen)) {
			ScreenOpener.open(parent);
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			ConfigHelper.changes.clear();
			ScreenOpener.open(parent);
		});
	}

	@Override
	public void onClose() {
		if (ConfigHelper.changes.isEmpty()) {
			super.onClose();
			return;
		}

		showLeavingPrompt(success -> {
			if (success == Response.Cancel)
				return;
			if (success == Response.Confirm)
				saveChanges();
			ConfigHelper.changes.clear();
			super.onClose();
		});
	}

	public void showLeavingPrompt(Consumer<ConfirmationScreen.Response> action) {
		ConfirmationScreen screen = new ConfirmationScreen()
				.centered()
				.withThreeActions(action)
				.addText(FormattedText.of("Leaving with " + ConfigHelper.changes.size() + " unsaved change"
						+ (ConfigHelper.changes.size() != 1 ? "s" : "") + " for this config"));

		addAnnotationsToConfirm(screen).open(this);
	}

	private ConfirmationScreen addAnnotationsToConfirm(ConfirmationScreen screen) {
		AtomicBoolean relog = new AtomicBoolean(false);
		AtomicBoolean restart = new AtomicBoolean(false);
		ConfigHelper.changes.values().forEach(change -> {
			if (change.annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
				relog.set(true);

			if (change.annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
				restart.set(true);
		});

		if (relog.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(FormattedText.of("At least one changed value will require you to relog to take full effect"));
		}

		if (restart.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(FormattedText.of("At least one changed value will require you to restart your game to take full effect"));
		}

		return screen;
	}

}
