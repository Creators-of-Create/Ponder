package net.createmod.catnip.impl.client.gui.config.entry.list;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractConfigEntryList<E extends AbstractConfigEntryList.Entry<E>> extends ContainerObjectSelectionList<E> {
	public static final String DEFAULT_FILTER = "";

	private String filter;

	public AbstractConfigEntryList(Minecraft minecraft, int width, int height, int top, int elementHeight) {
		super(minecraft, width, height, top, elementHeight);
		this.filter = DEFAULT_FILTER;
	}

	protected abstract void fillEntries();

	public final void updateFilter(String filter) {
		if (!this.filter.equals(filter)) {
			this.filter = filter;
			this.fillEntries();
		}
	}

	public final String filter() {
		return this.filter;
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
		Color c = new Color(0x60_000000);
		UIRenderHelper.angledGradient(graphics, 90, getX() + width / 2, getY(), width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, -90, getX() + width / 2, getBottom(), width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 0, getX(), getY() + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 180, getRight(), getY() + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);
	}

	@Override
	public int getRowWidth() {
		return this.width - 16;
	}

	public abstract static class Entry<E extends AbstractConfigEntryList.Entry<E>> extends ContainerObjectSelectionList.Entry<E> {
		protected static final float labelWidthMult = 0.4f;

		protected final String label;
		private final TextStencilElement labelElement;
		private final List<Component> tooltip;

		protected Entry(String label) {
			this.label = label;
			this.labelElement = new TextStencilElement(Minecraft.getInstance().font, label);
			this.labelElement.withElementRenderer((graphics, width, height, _) -> UIRenderHelper.angledGradient(graphics, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_STRONG_ACCENT));

			List<Component> tooltip = new ArrayList<>();
			this.buildTooltip(tooltip::add);
			this.tooltip = Collections.unmodifiableList(tooltip);
		}

		protected abstract void buildTooltip(Consumer<Component> output);

		protected abstract void collectChildren(Consumer<GuiEventListener> output);

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			UIRenderHelper.streak(graphics, 0, getX() - 10, getY() + getHeight() / 2, getHeight() - 6, getWidth() / 8 * 7, new Color(0xdd_000000));
			UIRenderHelper.streak(graphics, 180, getX() + (int) (getWidth() * 1.35f) + 10, getY() + getHeight() / 2, getHeight() - 6, getWidth() / 8 * 7, new Color(0xdd_000000));
			MutableComponent component = this.labelElement.getComponent();
			Font font = Minecraft.getInstance().font;
			if (font.width(component) > getLabelWidth(getWidth()) - 10) {
				this.labelElement.withText(font.substrByWidth(component, getLabelWidth(getWidth()) - 15).getString() + "...");
			}

			this.labelElement.at(getX() + 10, getY() + getHeight() / 2f - this.labelYOffset(), 0).submit(graphics);

			if (mouseX > getX() && mouseX < getX() + getLabelWidth(getWidth()) && mouseY > getY() + 5 && mouseY < getY() + getHeight() - 5) {
				if (this.tooltip.isEmpty())
					return;

				RenderSystem.disableScissorForRenderTypeDraws(); // TODO - Check if this is correct
				graphics.pose().pushMatrix();
				graphics.setComponentTooltipForNextFrame(font, this.tooltip, mouseX, mouseY);
				//graphics.flush(); TODO - Is there an replacement?
				//RemovedGuiUtils.drawHoveringText(ms, tooltip, mouseX, mouseY, screen.width, screen.height, 300, font);
				graphics.pose().popMatrix();
				GlStateManager._enableScissorTest();
			}
		}

		@Override
		public final List<? extends GuiEventListener> children() {
			List<GuiEventListener> list = new ArrayList<>();
			this.collectChildren(list::add);
			return list;
		}

		@Override
		public final List<? extends NarratableEntry> narratables() {
			List<NarratableEntry> list = new ArrayList<>();

			this.collectChildren(child -> {
				if (child instanceof NarratableEntry narratable) {
					list.add(narratable);
				}
			});

			return list;
		}

		protected int labelYOffset() {
			return 4;
		}

		protected int getLabelWidth(int totalWidth) {
			return (int) (totalWidth * labelWidthMult) + 30;
		}
	}
}
