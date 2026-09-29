package net.createmod.catnip.impl.client.gui.config.entry.list;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.catnip.impl.client.gui.config.RootConfigScreen;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class ModEntryList extends AbstractConfigEntryList<ModEntryList.Entry> {
	public ModEntryList(Minecraft minecraft, int width, int height, int top, int elementHeight) {
		super(minecraft, width, height, top, elementHeight);
	}

	@Override
	protected void fillEntries() {

	}

	public static final class Entry extends AbstractConfigEntryList.Entry<Entry> {
		private final BoxWidget button;
		private final String id;

		public Entry(String id, Screen parent) {
			super(PlatformHelper.INSTANCE.getModDisplayName(id));
			this.id = id;

			button = new BoxWidget(0, 0, 35, 16)
				.showingElement(CatnipGuiTextures.ICON_CONFIG_OPEN.asStencil().at(10, 0));
			button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(button)));

			if (ConfigHelper.hasAnyConfigs(id)) {
				button.withCallback(() -> RootConfigScreen.forMod(id, parent).ifPresent(ScreenOpener::open));
			} else {
				button.active = false;
				button.updateGradientFromState();
				button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(RootConfigScreen.DISABLED_RENDERER));
			}
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

			button.setX(getX() + getWidth() - 108);
			button.setY(getY() + 10);
			button.setHeight(getHeight() - 20);
			button.extractRenderState(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		protected void collectChildren(Consumer<GuiEventListener> output) {
			output.accept(this.button);
		}

		@Override
		protected void buildTooltip(Consumer<Component> output) {
			output.accept(Component.literal(PlatformHelper.INSTANCE.getModDisplayName(id)));

			if (!this.button.active) {
				FontHelper.cutTextComponent(Component.translatable("catnip.ui.other_mods_config_unavailable"), Palette.ALL_GRAY).forEach(output);
			}

			output.accept(Component.literal(this.id).withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
