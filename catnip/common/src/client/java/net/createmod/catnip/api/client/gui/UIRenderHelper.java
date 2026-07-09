package net.createmod.catnip.api.client.gui;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Constants;

import net.createmod.catnip.api.client.gui.render.BreadcrumbArrowRenderState;
import net.createmod.catnip.api.client.gui.render.GradientRectRenderState;
import net.createmod.catnip.api.client.gui.render.RadialSectorRenderState;
import net.createmod.catnip.api.client.gui.render.TexturedQuadRenderState;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.util.Mth;

public class UIRenderHelper {
	public static final Couple<Color> COLOR_TEXT = Couple.create(
		new Color(0xff_eeeeee),
		new Color(0xff_a3a3a3)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_TEXT_DARKER = Couple.create(
		new Color(0xff_a3a3a3),
		new Color(0xff_808080)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_TEXT_ACCENT = Couple.create(
		new Color(0xff_ddeeff),
		new Color(0xff_a0b0c0)
	).map(Color::setImmutable);
	public static final Couple<Color> COLOR_TEXT_STRONG_ACCENT = Couple.create(
		new Color(0xff_8ab6d6),
		new Color(0xff_6e92ab)
	).map(Color::setImmutable);

	public static final Color COLOR_STREAK = new Color(0x101010, false).setImmutable();

	/**
	 * @param angle   angle in degrees, 0 means fading to the right
	 * @param x       x-position of the starting edge middle point
	 * @param y       y-position of the starting edge middle point
	 * @param breadth total width of the streak
	 * @param length  total length of the streak
	 */
	public static void streak(GuiGraphicsExtractor graphics, float angle, int x, int y, int breadth, int length) {
		streak(graphics, angle, x, y, breadth, length, COLOR_STREAK);
	}

	public static void streak(GuiGraphicsExtractor graphics, float angle, int x, int y, int breadth, int length, Color c) {
		Color color = c.copy().setImmutable();
		Color c1 = color.scaleAlpha(0.625f);
		Color c2 = color.scaleAlpha(0.5f);
		Color c3 = color.scaleAlpha(0.0625f);
		Color c4 = color.scaleAlpha(0f);

		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(x, y);
		poseStack.rotate((angle - 90) * Constants.DEG_TO_RAD);

		streak(graphics, breadth / 2, length, c1, c2, c3, c4);

		poseStack.popMatrix();
	}

	private static void streak(GuiGraphicsExtractor graphics, int width, int height, Color c1, Color c2, Color c3, Color c4) {
		double split1 = .5;
		double split2 = .75;
		graphics.fillGradient(-width, 0, width, (int) (split1 * height), c1.getRGB(), c2.getRGB());
		graphics.fillGradient(-width, (int) (split1 * height), width, (int) (split2 * height), c2.getRGB(), c3.getRGB());
		graphics.fillGradient(-width, (int) (split2 * height), width, height, c3.getRGB(), c4.getRGB());
	}

	/**
	 * @see #angledGradient(GuiGraphicsExtractor, float, int, int, float, float, Color, Color)
	 */
	public static void angledGradient(GuiGraphicsExtractor graphics, float angle, int x, int y, float breadth, float length, Couple<Color> c) {
		angledGradient(graphics, angle, x, y, breadth, length, c.getFirst(), c.getSecond());
	}

	/**
	 * x and y specify the middle point of the starting edge
	 *
	 * @param angle      the angle of the gradient in degrees; 0° means from left to right
	 * @param startColor the color at the starting edge
	 * @param endColor   the color at the ending edge
	 * @param breadth    the total width of the gradient
	 */
	public static void angledGradient(GuiGraphicsExtractor graphics, float angle, int x, int y, float breadth, float length, Color startColor, Color endColor) {
		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(x, y);
		poseStack.rotate((angle - 90) * Constants.DEG_TO_RAD);

		float w = breadth / 2;
		//graphics.fillGradient(-w, 0, w, length, startColor.getRGB(), endColor.getRGB());
		drawGradientRect(graphics, -w, 0f, w, length, startColor, endColor);

		poseStack.popMatrix();
	}

	public static void drawGradientRect(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom, Color startColor, Color endColor) {
		graphics.guiRenderState.addGuiElement(new GradientRectRenderState(
			new Matrix3x2f(graphics.pose()),
			left,
			top,
			right,
			bottom,
			startColor,
			endColor
		));
	}

	public static void breadcrumbArrow(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int indent, Couple<Color> colors) {
		breadcrumbArrow(graphics, x, y, width, height, indent, colors.getFirst(), colors.getSecond());
	}

	/// Draws a wide chevron-style breadcrumb arrow pointing left.
	public static void breadcrumbArrow(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int indent, Color startColor, Color endColor) {
		Matrix3x2fStack transforms = graphics.pose();
		transforms.pushMatrix();
		transforms.translate(x - indent, y);

		graphics.guiRenderState.addGuiElement(new BreadcrumbArrowRenderState(
			new Matrix3x2f(transforms),
			width, height,
			indent, startColor, endColor,
			getScissor(graphics)
		));

		transforms.popMatrix();
	}

	/**
	 * centered on 0, 0
	 *
	 * @param arcAngle length of the sector arc
	 */
	public static void drawRadialSector(GuiGraphicsExtractor graphics, float innerRadius, float outerRadius, float startAngle, float arcAngle, Color innerColor, Color outerColor) {
		graphics.guiRenderState.addGuiElement(new RadialSectorRenderState(
			new Matrix3x2f(graphics.pose()),
			innerRadius,
			outerRadius,
			startAngle,
			arcAngle,
			innerColor,
			outerColor
		));
	}

	//just like AbstractGui#drawTexture, but with a color at every vertex
	public static void drawColoredTexture(GuiGraphicsExtractor graphics, TextureSetup texture, Color c, int x, int y, int texLeft, int texTop, int width, int height) {
		drawColoredTexture(graphics, texture, c, x, y, (float) texLeft, (float) texTop, width, height, 256, 256);
	}

	public static void drawColoredTexture(GuiGraphicsExtractor graphics, TextureSetup texture, Color c, int x, int y, float texLeft, float texTop, int width, int height, int sheetWidth, int sheetHeight) {
		//noinspection SuspiciousNameCombination
		drawColoredTexture(graphics, texture, c, x, x + width, y, y + height, width, height, texLeft, texTop, sheetWidth, sheetHeight);
	}

	public static void drawStretched(GuiGraphicsExtractor graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
		drawTexturedQuad(
			graphics, tex.bind(), Color.WHITE, left, left + w, top, top + h,
			tex.getStartX() / 256f, (tex.getStartX() + tex.getWidth()) / 256f,
			tex.getStartY() / 256f, (tex.getStartY() + tex.getHeight()) / 256f
		);
	}

	public static void drawStretched(GuiGraphicsExtractor graphics, int left, int top, int w, int h, int z, TextureSheetSegment tex) {
		drawStretched(graphics, left, top, w, h, tex);
	}

	public static void drawCropped(GuiGraphicsExtractor graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
		drawTexturedQuad(
			graphics, tex.bind(), Color.WHITE, left, left + w, top, top + h,
			tex.getStartX() / 256f, (tex.getStartX() + w) / 256f,
			tex.getStartY() / 256f, (tex.getStartY() + h) / 256f
		);
	}

	public static void drawCropped(GuiGraphicsExtractor graphics, int left, int top, int w, int h, int z, TextureSheetSegment tex) {
		drawCropped(graphics, left, top, w, h, tex);
	}

	private static void drawColoredTexture(GuiGraphicsExtractor graphics, TextureSetup texture, Color c, int left, int right, int top, int bot, int texWidth, int texHeight, float texLeft, float texRight, int sheetWidth, int sheetHeight) {
		drawTexturedQuad(graphics, texture, c, left, right, top, bot, (texLeft + 0.0F) / (float) sheetWidth, (texLeft + (float) texWidth) / (float) sheetWidth, (texRight + 0.0F) / (float) sheetHeight, (texRight + (float) texHeight) / (float) sheetHeight);
	}

	private static void drawTexturedQuad(GuiGraphicsExtractor graphics, TextureSetup texture, Color c, int left, int right, int top, int bot, float u1, float u2, float v1, float v2) {
		graphics.guiRenderState.addGuiElement(new TexturedQuadRenderState(
			new Matrix3x2f(graphics.pose()),
			getScissor(graphics),
			texture,
			c,
			left,
			right,
			top,
			bot,
			u1,
			u2,
			v1,
			v2
		));
	}

	public static void flipForGuiRender(PoseStack poseStack) {
		poseStack.mulPose(new Matrix4f().scaling(1, -1, 1));
	}

	public static void flipForGuiRender(Matrix3x2fStack poseStack) {
		poseStack.scale(-1, -1);
		poseStack.rotate(180 * Mth.DEG_TO_RAD);
	}

	/// @return the current scissor rectangle, if present
	@Nullable
	public static ScreenRectangle getScissor(GuiGraphicsExtractor graphics) {
		return null;
	}

	/// Compute the bounds of a GUI element.
	/// @see ColoredRectangleRenderState#getBounds(int, int, int, int, Matrix3x2fc, ScreenRectangle)
	@SuppressWarnings("JavadocReference")
	public static ScreenRectangle getBounds(ScreenRectangle bounds, Matrix3x2f pose, @Nullable ScreenRectangle scissor) {
		bounds = bounds.transformMaxBounds(pose);

		if (scissor != null) {
			bounds = bounds.intersection(scissor);
		}

		return bounds;
	}
}
