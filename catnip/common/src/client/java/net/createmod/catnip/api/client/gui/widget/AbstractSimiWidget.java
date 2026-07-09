package net.createmod.catnip.api.client.gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import net.createmod.catnip.api.client.gui.TickableGuiEventListener;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiWidget extends AbstractWidget implements TickableGuiEventListener {
	public static final Color HEADER_RGB = new Color(0x5391e1, false);
	public static final Color HINT_RGB = new Color(0x96b7e0, false);

	public static final Couple<Color> COLOR_IDLE = Couple.create(
		new Color(0xdd_8ab6d6, true),
		new Color(0x90_8ab6d6, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_HOVER = Couple.create(
		new Color(0xff_9abbd3, true),
		new Color(0xd0_9abbd3, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_CLICK = Couple.create(
		new Color(0xff_ffffff, true),
		new Color(0xee_ffffff, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_DISABLED = Couple.create(
		new Color(0x80_909090, true),
		new Color(0x60_909090, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_SUCCESS = Couple.create(
		new Color(0xcc_88f788, true),
		new Color(0xcc_20cc20, true)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_FAIL = Couple.create(
		new Color(0xcc_f78888, true),
		new Color(0xcc_cc2020, true)
	).map(Color::setImmutable);

	protected float z; // TODO - Check if this is needed anymore, remove if not
	protected boolean wasHovered = false;
	protected List<Component> toolTip = new LinkedList<>();
	// TODO - Look into changing this to MouseButtonEvent
	protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {
	};

	public int lockedTooltipX = -1;
	public int lockedTooltipY = -1;

	protected AbstractSimiWidget(int x, int y) {
		this(x, y, 16, 16);
	}

	protected AbstractSimiWidget(int x, int y, int width, int height) {
		this(x, y, width, height, CommonComponents.EMPTY);
	}

	protected AbstractSimiWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	public <T extends AbstractSimiWidget> T withCallback(BiConsumer<Integer, Integer> cb) {
		this.onClick = cb;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends AbstractSimiWidget> T withCallback(Runnable cb) {
		return withCallback((_$, _$$) -> cb.run());
	}

	public <T extends AbstractSimiWidget> T atZLevel(float z) {
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends AbstractSimiWidget> T setActive(boolean active) {
		this.active = active;
		return (T) this;
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	@Override
	public void tick() {
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		this.isHovered &= this.isActive();
		this.beforeRender(graphics, mouseX, mouseY, partialTicks);
		this.doRender(graphics, mouseX, mouseY, partialTicks);
		this.afterRender(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY, partialTicks);
		this.wasHovered = this.isHoveredOrFocused();
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	protected void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.isHovered()) {
			List<Component> tooltip = this.getToolTip();
			if (tooltip.isEmpty())
				return;
			int ttx = this.lockedTooltipX == -1 ? mouseX : this.lockedTooltipX + this.getX();
			int tty = this.lockedTooltipY == -1 ? mouseY : this.lockedTooltipY + this.getY();

			Font font = Minecraft.getInstance().font;
			graphics.setComponentTooltipForNextFrame(font, tooltip, ttx, tty);
		}
	}

	protected void beforeRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose().pushMatrix();
	}

	protected void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
	}

	protected void afterRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose().popMatrix();
	}

	public void runCallback(double mouseX, double mouseY) {
		onClick.accept((int) mouseX, (int) mouseY);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		runCallback(event.x(), event.y());
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}

	public void setHeight(int value) {
		this.height = value;
	}
}
