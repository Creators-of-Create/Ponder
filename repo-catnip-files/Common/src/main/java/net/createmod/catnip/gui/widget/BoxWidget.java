package net.createmod.catnip.gui.widget;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.FadableScreenElement;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.catnip.utility.theme.Theme.Key;

public class BoxWidget extends ElementWidget {

	public static final Function<BoxWidget, FadableScreenElement> gradientFactory = (box) -> (ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, w/2, -2, w + 4, h + 4, box.gradientColor1, box.gradientColor2);

	protected BoxElement box;

	@Nullable protected Color customBorderTop;
	@Nullable protected Color customBorderBot;
	@Nullable protected Color customBackground;
	protected Theme.Key disabledTheme = Theme.Key.BUTTON_DISABLE;
	protected Theme.Key idleTheme = Theme.Key.BUTTON_IDLE;
	protected Theme.Key hoverTheme = Theme.Key.BUTTON_HOVER;
	protected Theme.Key clickTheme = Theme.Key.BUTTON_CLICK;
	protected boolean animateColors = true;
	protected LerpedFloat colorAnimation = LerpedFloat.linear();

	protected Color gradientColor1, gradientColor2;
	private Color previousColor1, previousColor2;
	private Color colorTarget1 = getIdleTheme().c(true).copy();
	private Color colorTarget2 = getIdleTheme().c(false).copy();

	public BoxWidget() {
		this(0, 0);
	}

	public BoxWidget(int x, int y) {
		this(x, y, 16, 16);
	}

	public BoxWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
		box = new BoxElement()
				.at(x, y)
				.withBounds(width, height);
		previousColor1 = gradientColor1 = colorTarget1;
		previousColor2 = gradientColor2 = colorTarget2;
	}

	public <T extends BoxWidget> T withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withBorderColors(Couple<Color> colors) {
		this.customBorderTop = colors.getFirst();
		this.customBorderBot = colors.getSecond();
		updateColorsFromState();
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withBorderColors(Color top, Color bot) {
		this.customBorderTop = top;
		this.customBorderBot = bot;
		updateColorsFromState();
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withCustomBackground(Color color) {
		this.customBackground = color;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withThemeKeys(
			@Nullable Theme.Key disabledTheme,
			@Nullable Theme.Key idleTheme,
			@Nullable Theme.Key hoverTheme,
			@Nullable Theme.Key clickTheme
	) {
		if (disabledTheme != null)
			this.disabledTheme = disabledTheme;

		if (idleTheme != null) {
			this.idleTheme = idleTheme;
			updateColorsFromState();
		}

		if (hoverTheme != null)
			this.hoverTheme = hoverTheme;

		if (clickTheme != null)
			this.clickTheme = clickTheme;

		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T animateColors(boolean b) {
		this.animateColors = b;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	public void tick() {
		super.tick();
		colorAnimation.tickChaser();
	}

	@Override
	public void onClick(double x, double y) {
		super.onClick(x, y);

		gradientColor1 = getClickTheme().c(true);
		gradientColor2 = getClickTheme().c(false);
		startGradientAnimation(getColorForState(true), getColorForState(false), true, 0.15);
	}

	@Override
	protected void beforeRender(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(ms, mouseX, mouseY, partialTicks);

		RenderSystem.enableDepthTest();

		if (isHovered != wasHovered) {
			startGradientAnimation(
					getColorForState(true),
					getColorForState(false),
					isHovered
			);
		}

		if (colorAnimation.settled()) {
			gradientColor1 = colorTarget1;
			gradientColor2 = colorTarget2;
		} else {
			float animationValue = 1 - Math.abs(colorAnimation.getValue(partialTicks));
			gradientColor1 = Color.mixColors(previousColor1, colorTarget1, animationValue);
			gradientColor2 = Color.mixColors(previousColor2, colorTarget2, animationValue);
		}

	}

	@Override
	public void renderButton(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		float fadeValue = fade.getValue(partialTicks);
		if (fadeValue < .1f)
			return;

		box.withAlpha(fadeValue);
		box.withBackground(customBackground != null ? customBackground : Key.BOX_BACKGROUND_TRANSPARENT.c())
				.gradientBorder(gradientColor1, gradientColor2)
				.at(x, y, z)
				.withBounds(width, height)
				.render(ms);

		super.renderButton(ms, mouseX, mouseY, partialTicks);

		wasHovered = isHovered;
	}

	@Override
	public boolean isMouseOver(double mX, double mY) {
		if (!active || !visible)
			return false;

		return
				x - 4 <= mX &&
				y - 4 <= mY &&
				mX <= x + 3 + width &&
				mY <= y + 3 + height;
		//using 3 instead of the "correct" 4 for the last two gives better results
	}

	public BoxElement getBox() {
		return box;
	}

	public void updateColorsFromState() {
		colorTarget1 = getColorForState(true);
		colorTarget2 = getColorForState(false);
	}

	public void animateGradientFromState() {
		startGradientAnimation(
				getColorForState(true),
				getColorForState(false),
				true
		);
	}

	protected void startGradientAnimation(Color c1, Color c2, boolean positive, double expSpeed) {
		if (!animateColors)
			return;

		colorAnimation.startWithValue(positive ? 1 : -1);
		colorAnimation.chase(0, expSpeed, LerpedFloat.Chaser.EXP);
		colorAnimation.tickChaser();

		previousColor1 = gradientColor1;
		previousColor2 = gradientColor2;

		colorTarget1 = c1;
		colorTarget2 = c2;
	}

	protected void startGradientAnimation(Color c1, Color c2, boolean positive) {
		startGradientAnimation(c1, c2, positive, 0.6);
	}

	protected Color getColorForState(boolean first) {
		if (!active)
			return getDisabledTheme().p().get(first);

		if (isHovered) {
			if (first)
				return customBorderTop != null ? customBorderTop.darker() : getHoverTheme().c(true);
			else
				return customBorderBot != null ? customBorderBot.darker() : getHoverTheme().c(false);
		}

		if (first)
			return customBorderTop != null ? customBorderTop : getIdleTheme().c(true);
		else
			return customBorderBot != null ? customBorderBot : getIdleTheme().c(false);
	}

	public Key getDisabledTheme() {
		return disabledTheme;
	}

	public Key getIdleTheme() {
		return idleTheme;
	}

	public Key getHoverTheme() {
		return hoverTheme;
	}

	public Key getClickTheme() {
		return clickTheme;
	}

}
