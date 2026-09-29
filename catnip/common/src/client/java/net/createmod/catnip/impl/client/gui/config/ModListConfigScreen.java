package net.createmod.catnip.impl.client.gui.config;

import java.util.Locale;

import net.createmod.catnip.impl.client.gui.config.entry.list.ModEntryList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.level.block.Blocks;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ModListConfigScreen extends AbstractConfigScreen {
	// FIXME: translation
	public static final Component TITLE = Component.literal("Mod List");
	public static final Component SEARCH = Component.literal("Search Mods");

	private @Nullable ModEntryList list;
	private @Nullable EditBox search;

	ModListConfigScreen(@Nullable Screen parent) {
		super(TITLE, parent, Blocks.CRIMSON_ROOTS.defaultBlockState());
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);

		list = new ModEntryList(minecraft, listWidth, height - 60, 15, 40);
		list.setX(this.width / 2 - list.getWidth() / 2);
		addRenderableWidget(list);

		BoxWidget goBack = new BoxWidget(width / 2 - listWidth / 2 - 30, height / 2 + 65, 20, 20).withPadding(2, 2)
			.withCallback(() -> ScreenOpener.open(parent));
		goBack.showingElement(CatnipGuiTextures.ICON_CONFIG_BACK.asStencil()
			.withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(goBack)));
		goBack.getToolTip()
			.add(Component.translatable("catnip.ui.go_back_button"));
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
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		// FIXME: consistency with config screen
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
}
