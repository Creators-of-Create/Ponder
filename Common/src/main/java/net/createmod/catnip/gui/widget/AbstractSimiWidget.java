package net.createmod.catnip.gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import net.createmod.catnip.gui.TickableGuiEventListener;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
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

	protected float z;
	protected boolean wasHovered = false;
	protected List<Component> toolTip = new LinkedList<>();
	protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {};

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
	public void tick() {}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = isMouseOver(mouseX, mouseY);
			renderWidget(graphics, mouseX, mouseY, partialTicks);
			wasHovered = isHoveredOrFocused();
		}
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		beforeRender(graphics, mouseX, mouseY, partialTicks);
		doRender(graphics, mouseX, mouseY, partialTicks);
		afterRender(graphics, mouseX, mouseY, partialTicks);
	}

	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose().pushPose();
	}

	protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

	protected void afterRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose().popPose();
	}

	public void runCallback(double mouseX, double mouseY) {
		onClick.accept((int) mouseX, (int) mouseY);
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return this.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		runCallback(mouseX, mouseY);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}

	public void setHeight(int value) {
		this.height = value;
	}
}
