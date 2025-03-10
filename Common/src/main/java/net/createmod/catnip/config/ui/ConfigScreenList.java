package net.createmod.catnip.config.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.gui.TickableGuiEventListener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfigScreenList extends ObjectSelectionList<ConfigScreenList.Entry> implements TickableGuiEventListener {

	@Nullable public static EditBox currentText;

	public ConfigScreenList(Minecraft client, int width, int height, int top, int bottom, int elementHeight) {
		super(client, width, height, top, bottom, elementHeight);
		setRenderBackground(false);
		setRenderTopAndBottom(false);
		setRenderSelection(false);
		currentText = null;
		headerHeight = 3;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		Color c = new Color(0x60_000000);
		UIRenderHelper.angledGradient(graphics, 90, x0 + width / 2, y0, width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, -90, x0 + width / 2, y1, width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 0, x0, y0 + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 180, x1, y0 + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);

		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Window window = minecraft.getWindow();
		double d0 = window.getGuiScale();
		RenderSystem.enableScissor((int) (this.x0 * d0), (int) (window.getHeight() - (this.y1 * d0)), (int) (this.width * d0), (int) (this.height * d0));
		super.renderList(graphics, mouseX, mouseY, partialTick);
		RenderSystem.disableScissor();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		//children().stream().filter(e -> e instanceof NumberEntry<?>).forEach(e -> e.mouseClicked(x, y, button));

		return super.mouseClicked(x, y, button);
	}

	@Override
	public int getRowWidth() {
		return width - 16;
	}

	public int getWidth() {
		return width;
	}

	@Override
	protected int getScrollbarPosition() {
		return x0 + this.width - 6;
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
		if (query == null || query.isEmpty()) {
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
		@Nullable protected String path;

		protected Entry() {
			listeners = new ArrayList<>();
			annotations = new HashMap<>();
		}

		@Override
		public boolean mouseClicked(double x, double y, int button) {
			return getGuiListeners().stream().anyMatch(l -> l.mouseClicked(x, y, button));
		}

		@Override
		public boolean keyPressed(int code, int keyPressed_2_, int keyPressed_3_) {
			return getGuiListeners().stream().anyMatch(l -> l.keyPressed(code, keyPressed_2_, keyPressed_3_));
		}

		@Override
		public boolean charTyped(char ch, int code) {
			for (GuiEventListener l : getGuiListeners()) {
				if (l.charTyped(ch, code)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public void tick() {}

		public List<GuiEventListener> getGuiListeners() {
			return listeners;
		}

		protected void setEditable(boolean b) {}

		protected boolean isCurrentValueChanged() {
			if (path == null) {
				return false;
			}
			return ConfigHelper.changes.containsKey(path);
		}
	}

	public static class LabeledEntry extends Entry {

		protected static final float labelWidthMult = 0.4f;

		protected TextStencilElement label;
		protected List<Component> labelTooltip;
		@Nullable protected String unit = null;
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
		public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			if (isCurrentValueChanged()) {
				if (differenceAnimation.getChaseTarget() != 1)
					differenceAnimation.chase(1, .5f, LerpedFloat.Chaser.EXP);
			} else {
				if (differenceAnimation.getChaseTarget() != 0)
					differenceAnimation.chase(0, .6f, LerpedFloat.Chaser.EXP);
			}

			float animation = differenceAnimation.getValue(partialTicks);
			if (animation > .1f) {
				int offset = (int) (30 * (1 - animation));

				if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName())) {
					UIRenderHelper.streak(graphics, 180, x + width + 10 + offset, y + height / 2, height - 6, 110, new Color(0x50_601010));
				} else if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName())) {
					UIRenderHelper.streak(graphics, 180, x + width + 10 + offset, y + height / 2, height - 6, 110, new Color(0x40_eefb17));
				}

				UIRenderHelper.breadcrumbArrow(graphics, x - 10 - offset, y + 6, 0, -20, 24, -18, new Color(0x70_ffffff), Color.TRANSPARENT_BLACK);
			}

			UIRenderHelper.streak(graphics, 0, x - 10, y + height / 2, height - 6, width / 8 * 7, new Color(0xdd_000000));
			UIRenderHelper.streak(graphics, 180, x + (int) (width * 1.35f) + 10, y + height / 2, height - 6, width / 8 * 7, new Color(0xdd_000000));
			MutableComponent component = label.getComponent();
			Font font = Minecraft.getInstance().font;
			if (font.width(component) > getLabelWidth(width) - 10) {
				label.withText(font.substrByWidth(component, getLabelWidth(width) - 15).getString() + "...");
			}
			if (unit != null) {
				int unitWidth = font.width(unit);
				graphics.drawString(font, unit, x + getLabelWidth(width) - unitWidth - 5, y + height / 2 + 2, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB());
				label.at(x + 10, y + height / 2 - 10, 0).render(graphics);
			} else {
				label.at(x + 10, y + height / 2 - 4, 0).render(graphics);
			}

			if (annotations.containsKey("highlight")) {
				highlightAnimation.startWithValue(1).chase(0, 0.1f, LerpedFloat.Chaser.LINEAR);
				annotations.remove("highlight");
			}

			animation = highlightAnimation.getValue(partialTicks);
			if (animation > .01f) {
				Color highlight = new Color(0xa0_ffffff).scaleAlpha(animation);
				UIRenderHelper.streak(graphics, 0, x - 10, y + height / 2, height - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 180, x + width, y + height / 2, height - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 90, x + width / 2 - 5, y + 3, width + 10, 5, highlight);
				UIRenderHelper.streak(graphics, -90, x + width / 2 - 5, y + height - 3, width + 10, 5, highlight);
			}


			if (mouseX > x && mouseX < x + getLabelWidth(width) && mouseY > y + 5 && mouseY < y + height - 5) {
				List<Component> tooltip = getLabelTooltip();
				if (tooltip.isEmpty())
					return;

				RenderSystem.disableScissor();
				graphics.pose().pushPose();
				graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
				graphics.flush();
				//RemovedGuiUtils.drawHoveringText(ms, tooltip, mouseX, mouseY, screen.width, screen.height, 300, font);
				graphics.pose().popPose();
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
