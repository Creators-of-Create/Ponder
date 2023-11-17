package net.createmod.ponder.foundation.ui;

import java.util.Locale;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme.Key;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class PonderButton extends BoxWidget {

	@Nullable protected ItemStack item;
	@Nullable protected PonderTag tag;
	@Nullable protected KeyMapping shortcut;
	protected LerpedFloat flash = LerpedFloat.linear().startWithValue(0).chase(0, 0.1f, LerpedFloat.Chaser.EXP);

	public PonderButton(int x, int y) {
		this(x, y, 20, 20);
	}

	public PonderButton(int x, int y, int width, int height) {
		super(x, y, width, height);
		z = 420;
		paddingX = 2;
		paddingY = 2;
		disabledTheme = PonderTheme.Key.PONDER_BUTTON_DISABLE;
		idleTheme = PonderTheme.Key.PONDER_BUTTON_IDLE;
		hoverTheme = PonderTheme.Key.PONDER_BUTTON_HOVER;
		clickTheme = PonderTheme.Key.PONDER_BUTTON_CLICK;
		updateColorsFromState();
	}

	public <T extends PonderButton> T withShortcut(KeyMapping key) {
		this.shortcut = key;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends PonderButton> T showingTag(PonderTag tag) {
		return showing(this.tag = tag);
	}

	public <T extends PonderButton> T showing(ItemStack item) {
		this.item = item;
		return super.showingElement(GuiGameElement.of(item)
				.scale(1.5f)
				.at(-4, -4));
	}

	public void flash() {
		flash.updateChaseTarget(1);
	}

	public void dim() {
		flash.updateChaseTarget(0);
	}

	@Override
	public void tick() {
		super.tick();
		flash.tickChaser();
	}

	@Override
	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(graphics, mouseX, mouseY, partialTicks);

		float flashValue = flash.getValue(partialTicks);
		if (flashValue > .1f) {
			float sin = 0.5f + 0.5f * Mth.sin((AnimationTickHolder.getTicks(true) + partialTicks) / 10f);
			sin *= flashValue;
			Color nc1 = new Color(255, 255, 255, Mth.clamp(gradientColor1.getAlpha() + 150, 0, 255));
			Color nc2 = new Color(155, 155, 155, Mth.clamp(gradientColor2.getAlpha() + 150, 0, 255));
			gradientColor1 = gradientColor1.mixWith(nc1, sin);
			gradientColor2 = gradientColor2.mixWith(nc2, sin);
		}
	}

	@Override
	public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.doRender(graphics, mouseX, mouseY, partialTicks);
		float fadeValue = fade.getValue();

		if (fadeValue < .1f)
			return;

		if (shortcut != null) {
			PoseStack poseStack = graphics.pose();
			poseStack.pushPose();
			poseStack.translate(0, 0, z + 10);
			graphics.drawCenteredString(Minecraft.getInstance().font, shortcut.getTranslatedKeyMessage().getString().toLowerCase(
					Locale.ROOT), getX() + width / 2 + 8, getY() + height - 6, Key.TEXT_DARKER.c().scaleAlpha(fadeValue).getRGB());
			poseStack.popPose();
		}
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (shortcut != null && shortcut.matches(keyCode, scanCode)) {

			gradientColor1 = getClickTheme().c(true);
			gradientColor2 = getClickTheme().c(false);
			startGradientAnimation(getColorForState(true), getColorForState(false), true, 0.15);

			runCallback(width / 2f, height / 2f);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Nullable
	public ItemStack getItem() {
		return item;
	}

	@Nullable
	public PonderTag getTag() {
		return tag;
	}
}
