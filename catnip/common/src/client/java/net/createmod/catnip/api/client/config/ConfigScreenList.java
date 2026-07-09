package net.createmod.catnip.api.client.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.createmod.catnip.api.client.config.ConfigAnnotations.RequiresRelog;
import net.createmod.catnip.api.client.config.ConfigAnnotations.RequiresRestart;
import net.createmod.catnip.api.client.gui.TickableGuiEventListener;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.TextStencilElement;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfigScreenList extends ObjectSelectionList<ConfigScreenList.Entry> implements TickableGuiEventListener {

	@Nullable
	public static EditBox currentText;

	public ConfigScreenList(Minecraft client, int width, int height, int top, int elementHeight) {
		super(client, width, height, top, elementHeight);
		currentText = null;
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		Color c = new Color(0x60_000000);
		UIRenderHelper.angledGradient(graphics, 90, getX() + width / 2, getY(), width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, -90, getX() + width / 2, getBottom(), width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 0, getX(), getY() + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 180, getRight(), getY() + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);

		super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	public void setEntries(Collection<? extends Entry> entries) {
		clearEntries();
		entries.forEach(this::addConfigEntry);
	}

	public void clearConfigEntries() {
		clearEntries();
	}

	public void addConfigEntry(Entry entry) {
		addEntry(entry);
	}

	public void sortEntries(Comparator<Entry> comparator) {
		sort(comparator);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		//children().stream().filter(e -> e instanceof NumberEntry<?>).forEach(e -> e.mouseClicked(buttonEvent, doubleClick));
		//children().stream().filter(e -> e instanceof StringEntry).forEach(e -> e.mouseClicked(buttonEvent, doubleClick));

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public int getRowWidth() {
		return width - 16;
	}

	@Override
	protected int scrollBarX() {
		return getX() + this.width - 6;
	}

	@Override
	public void tick() {
		/*for(int i = 0; i < getItemCount(); ++i) {
			int top = this.getRowTop(i);
			int bot = top + itemHeight;
			if (bot >= this.y0 && top <= this.y1)
				this.getEntry(i).tick();
		}*/
		children().forEach(Entry::tick);

	}

	public boolean search(String query) {
		if (query.isEmpty()) {
			setScrollAmount(0);
			return true;
		}

		String q = query.toLowerCase(Locale.ROOT);
		Optional<Entry> first = children().stream().filter(entry -> {
			if (entry.path == null)
				return false;

			String[] split = entry.path.split("\\.");
			String key = split[split.length - 1].toLowerCase(Locale.ROOT);
			return key.contains(q);
		}).findFirst();

		if (first.isEmpty()) {
			setScrollAmount(0);
			return false;
		}

		Entry e = first.get();
		e.annotations.put("highlight", "(:");
		centerScrollOn(e);
		return true;
	}

	public void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}

	public static abstract class Entry extends ObjectSelectionList.Entry<Entry> implements TickableGuiEventListener {
		protected List<GuiEventListener> listeners;
		protected Map<String, String> annotations;
		@Nullable
		protected String path;

		protected Entry() {
			listeners = new ArrayList<>();
			annotations = new HashMap<>();
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			return getGuiListeners().stream().anyMatch(l -> l.mouseClicked(event, doubleClick));
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			return getGuiListeners().stream().anyMatch(l -> l.keyPressed(event));
		}

		@Override
		public boolean charTyped(CharacterEvent event) {
			for (GuiEventListener l : getGuiListeners()) {
				if (l.charTyped(event)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public void tick() {
		}

		public List<GuiEventListener> getGuiListeners() {
			return listeners;
		}

		protected void setEditable(boolean b) {
		}

		protected boolean isCurrentValueChanged() {
			if (path == null) {
				return false;
			}
			return net.createmod.catnip.api.config.ConfigHelper.changes.containsKey(path);
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			renderContent(graphics, mouseX, mouseY, hovered, partialTick);
		}

		public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		}
	}

	public static class LabeledEntry extends Entry {
		protected static final float labelWidthMult = 0.4f;

		protected TextStencilElement label;
		protected List<Component> labelTooltip;
		@Nullable
		protected String unit = null;
		protected LerpedFloat differenceAnimation = LerpedFloat.linear().startWithValue(0);
		protected LerpedFloat highlightAnimation = LerpedFloat.linear().startWithValue(0);

		public LabeledEntry(String label) {
			this.label = new TextStencilElement(Minecraft.getInstance().font, label);
			this.label.withElementRenderer((graphics, width, height, alpha) -> UIRenderHelper.angledGradient(graphics, 0, 0, height / 2, height, width, UIRenderHelper.COLOR_TEXT_STRONG_ACCENT));
			labelTooltip = new ArrayList<>();
		}

		public LabeledEntry(String label, String path) {
			this(label);
			this.path = path;
		}

		@Override
		public void tick() {
			differenceAnimation.tickChaser();
			highlightAnimation.tickChaser();
			super.tick();
		}

		@Override
		public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
			if (isCurrentValueChanged()) {
				if (differenceAnimation.getChaseTarget() != 1)
					differenceAnimation.chase(1, .5f, Chaser.EXP);
			} else {
				if (differenceAnimation.getChaseTarget() != 0)
					differenceAnimation.chase(0, .6f, Chaser.EXP);
			}

			float animation = differenceAnimation.getValue(partialTick);
			if (animation > .1f) {
				int offset = (int) (30 * (1 - animation));

				if (annotations.containsKey(RequiresRestart.CLIENT.getName())) {
					UIRenderHelper.streak(graphics, 180, getX() + getWidth() + 10 + offset, getY() + getHeight() / 2, getHeight() - 6, 110, new Color(0x50_601010));
				} else if (annotations.containsKey(RequiresRelog.TRUE.getName())) {
					UIRenderHelper.streak(graphics, 180, getX() + getWidth() + 10 + offset, getY() + getHeight() / 2, getHeight() - 6, 110, new Color(0x40_eefb17));
				}

				UIRenderHelper.breadcrumbArrow(graphics, getX() - 10 - offset, getY() + 6, -20, 24, -18, new Color(0x70_ffffff), Color.TRANSPARENT_BLACK);
			}

			UIRenderHelper.streak(graphics, 0, getX() - 10, getY() + getHeight() / 2, getHeight() - 6, getWidth() / 8 * 7, new Color(0xdd_000000));
			UIRenderHelper.streak(graphics, 180, getX() + (int) (getWidth() * 1.35f) + 10, getY() + getHeight() / 2, getHeight() - 6, getWidth() / 8 * 7, new Color(0xdd_000000));
			MutableComponent component = label.getComponent();
			Font font = Minecraft.getInstance().font;
			if (font.width(component) > getLabelWidth(getWidth()) - 10) {
				label.withText(font.substrByWidth(component, getLabelWidth(getWidth()) - 15).getString() + "...");
			}
			if (unit != null) {
				int unitWidth = font.width(unit);
				graphics.text(font, unit, getX() + getLabelWidth(getWidth()) - unitWidth - 5, getY() + getHeight() / 2 + 2, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB());
				label.at(getX() + 10, getY() + getHeight() / 2f - 10, 0).submit(graphics);
			} else {
				label.at(getX() + 10, getY() + getHeight() / 2f - 4, 0).submit(graphics);
			}

			if (annotations.containsKey("highlight")) {
				highlightAnimation.startWithValue(1).chase(0, 0.1f, Chaser.LINEAR);
				annotations.remove("highlight");
			}

			animation = highlightAnimation.getValue(partialTick);
			if (animation > .01f) {
				Color highlight = new Color(0xa0_ffffff).scaleAlpha(animation);
				UIRenderHelper.streak(graphics, 0, getX() - 10, getY() + getHeight() / 2, getHeight() - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 180, getX() + getWidth(), getY() + getHeight() / 2, getHeight() - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 90, getX() + getWidth() / 2 - 5, getY() + 3, getWidth() + 10, 5, highlight);
				UIRenderHelper.streak(graphics, -90, getX() + getWidth() / 2 - 5, getY() + getHeight() - 3, getWidth() + 10, 5, highlight);
			}


			if (mouseX > getX() && mouseX < getX() + getLabelWidth(getWidth()) && mouseY > getY() + 5 && mouseY < getY() + getHeight() - 5) {
				List<Component> tooltip = getLabelTooltip();
				if (tooltip.isEmpty())
					return;

				RenderSystem.disableScissorForRenderTypeDraws(); // TODO - Check if this is correct
				graphics.pose().pushMatrix();
				graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
				//graphics.flush(); TODO - Is there an replacement?
				//RemovedGuiUtils.drawHoveringText(ms, tooltip, mouseX, mouseY, screen.width, screen.height, 300, font);
				graphics.pose().popMatrix();
				GlStateManager._enableScissorTest();
			}
		}


		public List<Component> getLabelTooltip() {
			return labelTooltip;
		}

		protected int getLabelWidth(int totalWidth) {
			return totalWidth;
		}

		// TODO 1.17
		@Override
		public Component getNarration() {
			return CommonComponents.EMPTY;
		}
	}
}
