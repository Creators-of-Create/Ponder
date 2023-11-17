package net.createmod.catnip.gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import net.createmod.catnip.gui.TickableGuiEventListener;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiWidget extends AbstractWidget implements TickableGuiEventListener {

	public static final int HEADER_RGB = 0x5391E1;
	public static final int HINT_RGB = 0x96B7E0;

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
		this(x, y, width, height, Components.immutableEmpty());
	}

	protected AbstractSimiWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Override
	protected ClientTooltipPositioner createTooltipPositioner() {
		return DefaultTooltipPositioner.INSTANCE;
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
