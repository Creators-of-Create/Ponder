package net.createmod.catnip.gui.widget;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.FadableScreenElement;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

public class BoxWidget extends ElementWidget {

	public static final Function<BoxWidget, FadableScreenElement> gradientFactory = (box) -> (ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, w/2, -2, w + 4, h + 4, box.gradientColor);

	protected BoxElement box;

	@Nullable protected Couple<Color> customBorder;
	@Nullable protected Color customBackground;
	protected Couple<Color> colorIdle = AbstractSimiWidget.COLOR_IDLE;
	protected Couple<Color> colorHover = AbstractSimiWidget.COLOR_HOVER;
	protected Couple<Color> colorClick = AbstractSimiWidget.COLOR_CLICK;
	protected Couple<Color> colorDisabled = AbstractSimiWidget.COLOR_DISABLED;
	protected boolean animateColors = true;
	protected LerpedFloat colorAnimation = LerpedFloat.linear();

	protected Couple<Color> gradientColor;
	private Couple<Color> previousGradient;
	private Couple<Color> gradientTarget;

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
		previousGradient = gradientColor = gradientTarget = getColorIdle();
	}

	public <T extends BoxWidget> T withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withBorderColors(Couple<Color> colors) {
		this.customBorder = colors;
		updateGradientFromState();
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withBorderColors(Color top, Color bot) {
		return this.withBorderColors(Couple.create(top, bot));
	}

	public <T extends BoxWidget> T withCustomBackground(Color color) {
		this.customBackground = color;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withCustomTheme(
		@Nullable Couple<Color> colorIdle,
		@Nullable Couple<Color> colorHover,
		@Nullable Couple<Color> colorClick,
		@Nullable Couple<Color> colorDisabled
	) {
		if (colorIdle != null)
			this.colorIdle = colorIdle;

		if (colorHover != null)
			this.colorIdle = colorHover;

		if (colorClick != null)
			this.colorIdle = colorClick;

		if (colorDisabled != null)
			this.colorIdle = colorDisabled;

		updateGradientFromState();
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

		gradientColor = getColorClick();
		startGradientAnimation(getColorForState(), 0.15);
	}

	@Override
	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(graphics, mouseX, mouseY, partialTicks);

		RenderSystem.enableDepthTest();

		if (isHovered != wasHovered) {
			animateGradientFromState();
		}

		if (colorAnimation.settled()) {
			gradientColor = gradientTarget;
		} else {
			float animationValue = 1 - Math.abs(colorAnimation.getValue(partialTicks));
			gradientColor = previousGradient.mapWithParams((prev, target) -> prev.mixWith(target, animationValue), gradientTarget);
		}

	}

	@Override
	public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		float fadeValue = fade.getValue(partialTicks);
		if (fadeValue < .1f)
			return;

		box.withAlpha(fadeValue);
		box.withBackground(customBackground != null ? customBackground : BoxElement.COLOR_BACKGROUND_TRANSPARENT)
				.gradientBorder(gradientColor)
				.at(getX(), getY(), z)
				.withBounds(width, height)
				.render(graphics);

		super.doRender(graphics, mouseX, mouseY, partialTicks);

		wasHovered = isHovered;
	}

	@Override
	public boolean isMouseOver(double mX, double mY) {
		if (!active || !visible)
			return false;

		float padX = 2 + paddingX;
		float padY = 2 + paddingY;

		return getX() - padX <= mX && getY() - padY <= mY && mX < getX() + padX + width && mY < getY() + padY + height;
	}

	@Override
	protected boolean clicked(double pMouseX, double pMouseY) {
		if (!active || !visible)
			return false;
		return isMouseOver(pMouseX, pMouseY);
	}

	public BoxElement getBox() {
		return box;
	}

	public void updateGradientFromState() {
		gradientTarget = getColorForState();
	}

	public void animateGradientFromState() {
		startGradientAnimation(getColorForState());
	}

	protected void startGradientAnimation(Couple<Color> target, double expSpeed) {
		if (!animateColors)
			return;

		colorAnimation.startWithValue(1);
		colorAnimation.chase(0, expSpeed, LerpedFloat.Chaser.EXP);
		colorAnimation.tickChaser();

		previousGradient = gradientColor;
		gradientTarget = target;
	}

	protected void startGradientAnimation(Couple<Color> target) {
		startGradientAnimation(target, 0.6);
	}

	protected Couple<Color> getColorForState() {
		if (!active)
			return getColorDisabled();

		if (customBorder != null)
			return isHovered ? customBorder.map(Color::darker) : customBorder;

		return isHovered ? getColorHover() : getColorIdle();
	}

	public Couple<Color> getColorIdle() {
		return colorIdle;
	}

	public Couple<Color> getColorHover() {
		return colorHover;
	}

	public Couple<Color> getColorClick() {
		return colorClick;
	}

	public Couple<Color> getColorDisabled() {
		return colorDisabled;
	}

}
