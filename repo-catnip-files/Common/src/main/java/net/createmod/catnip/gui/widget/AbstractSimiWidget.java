package net.createmod.catnip.gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.TickableGuiEventListener;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = isMouseOver(mouseX, mouseY);
			beforeRender(ms, mouseX, mouseY, partialTicks);
			renderButton(ms, mouseX, mouseY, partialTicks);
			afterRender(ms, mouseX, mouseY, partialTicks);
			wasHovered = isHoveredOrFocused();
		}
	}

	protected void beforeRender(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.pushPose();
	}

	@Override
	public void renderButton(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
	}

	protected void afterRender(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.popPose();
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
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}

	public void setHeight(int value) {
		this.height = value;
	}
}
