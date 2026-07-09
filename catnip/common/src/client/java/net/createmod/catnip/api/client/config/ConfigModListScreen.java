package net.createmod.catnip.api.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ConfigModListScreen extends ConfigScreen {

	@Nullable
	ConfigScreenList list;
	@Nullable
	HintableTextFieldWidget search;
	@Nullable
	BoxWidget goBack;
	List<ModEntry> allEntries = new ArrayList<>();

	public ConfigModListScreen(@Nullable Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);

		list = new ConfigScreenList(minecraft, listWidth, height - 60, 15, 40);
		list.setX(this.width / 2 - list.getWidth() / 2);
		addRenderableWidget(list);

		allEntries = new ArrayList<>();
		PlatformHelper.INSTANCE.getLoadedMods().forEach(id -> allEntries.add(new ModEntry(id, this)));
		allEntries.sort((e1, e2) -> {
			int empty = (e2.button.active ? 1 : 0) - (e1.button.active ? 1 : 0);
			if (empty != 0)
				return empty;

			return e1.id.compareToIgnoreCase(e2.id);
		});
		list.setEntries(allEntries);

		goBack = new BoxWidget(width / 2 - listWidth / 2 - 30, height / 2 + 65, 20, 20).withPadding(2, 2)
			.withCallback(() -> ScreenOpener.open(parent));
		goBack.showingElement(CatnipGuiTextures.ICON_CONFIG_BACK.asStencil()
			.withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip()
			.add(Component.translatable("catnip.ui.go_back_button"));
		addRenderableWidget(goBack);

		search = new HintableTextFieldWidget(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint(Component.translatable("catnip.ui.search_hint"));
		search.moveCursorToStart(false);
		addRenderableWidget(search);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (search != null && !search.isMouseOver(event.x(), event.y()))
			search.setFocused(false);

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (super.keyPressed(keyEvent))
			return true;

		if (search != null && keyEvent.hasControlDown()) {
			if (keyEvent.key() == InputConstants.KEY_F) {
				this.setFocused(search);
			}
		}

		if (keyEvent.key() == InputConstants.KEY_BACKSPACE) {
			ScreenOpener.open(parent);
		}
		return false;
	}

	private void updateFilter(String search) {
		assert list != null;
		assert this.search != null;

		list.clearConfigEntries();
		//todo include display names in search
		for (ModEntry modEntry : allEntries) {
			if (modEntry.id.contains(search.toLowerCase(Locale.ROOT))) {
				list.addConfigEntry(modEntry);
			}
		}

		list.setScrollAmount(list.scrollAmount());
		if (!list.children().isEmpty()) {
			this.search.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		} else {
			this.search.setTextColor(AbstractSimiWidget.COLOR_FAIL.getFirst().getRGB());
		}
	}

	public static class ModEntry extends ConfigScreenList.LabeledEntry {

		protected BoxWidget button;
		protected String id;

		public ModEntry(String id, Screen parent) {
			super(PlatformHelper.INSTANCE.getModDisplayName(id));
			this.id = id;

			button = new BoxWidget(0, 0, 35, 16)
				.showingElement(CatnipGuiTextures.ICON_CONFIG_OPEN.asStencil().at(10, 0));
			button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(button)));

			if (net.createmod.catnip.api.config.ConfigHelper.hasAnyForgeConfig(id)) {
				button.withCallback(() -> ScreenOpener.open(new BaseConfigScreen(parent, id)));
			} else {
				button.active = false;
				button.updateGradientFromState();
				button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BaseConfigScreen.DISABLED_RENDERER));
				labelTooltip.add(Component.literal(PlatformHelper.INSTANCE.getModDisplayName(id)));
				labelTooltip.addAll(FontHelper.cutTextComponent(Component.translatable("catnip.ui.other_mods_config_unavailable"), Palette.ALL_GRAY));
			}

			listeners.add(button);
		}

		public String getId() {
			return id;
		}

		@Override
		public void tick() {
			super.tick();
			button.tick();
		}

		@Override
		public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
			super.renderContent(graphics, mouseX, mouseY, isHovering, partialTick);

			button.setX(getX() + getWidth() - 108);
			button.setY(getY() + 10);
			button.setHeight(getHeight() - 20);
			button.extractRenderState(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		protected int getLabelWidth(int totalWidth) {
			return (int) (totalWidth * labelWidthMult) + 30;
		}
	}
}
