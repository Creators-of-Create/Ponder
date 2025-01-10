package net.createmod.catnip.gui;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;

import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
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
	 * An FBO that has a stencil buffer for use wherever stencil are necessary. Forcing the main FBO to have a stencil
	 * buffer will cause GL error spam when using fabulous graphics.
	 */
	@Nullable
	public static CustomRenderTarget framebuffer;

	public static void init() {
		RenderSystem.recordRenderCall(() -> {
			Window mainWindow = Minecraft.getInstance().getWindow();
			framebuffer = CustomRenderTarget.create(mainWindow);
		});
	}

	public static void updateWindowSize(Window mainWindow) {
		if (framebuffer != null)
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
	}

	public static void drawFramebuffer(PoseStack poseStack, float alpha) {
		if (framebuffer != null)
			framebuffer.renderWithAlpha(poseStack, alpha);
	}

	/**
	 * Switch from src to dst, after copying the contents of src to dst.
	 */
	public static void swapAndBlitColor(RenderTarget src, RenderTarget dst) {
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.frameBufferId);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dst.frameBufferId);
		GlStateManager._glBlitFrameBuffer(0, 0, src.viewWidth, src.viewHeight, 0, 0, dst.viewWidth, dst.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, dst.frameBufferId);
	}

	/**
	 * @param angle angle in degrees, 0 means fading to the right
	 * @param x x-position of the starting edge middle point
	 * @param y y-position of the starting edge middle point
	 * @param breadth total width of the streak
	 * @param length total length of the streak
	 */
	public static void streak(GuiGraphics graphics, float angle, int x, int y, int breadth, int length) {
		streak(graphics, angle, x, y, breadth, length, COLOR_STREAK);
	}

	public static void streak(GuiGraphics graphics, float angle, int x, int y, int breadth, int length, Color c) {
		Color color = c.copy().setImmutable();
		Color c1 = color.scaleAlpha(0.625f);
		Color c2 = color.scaleAlpha(0.5f);
		Color c3 = color.scaleAlpha(0.0625f);
		Color c4 = color.scaleAlpha(0f);

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x, y, 0);
		poseStack.mulPose(Axis.ZP.rotationDegrees(angle - 90));

		streak(graphics, breadth / 2, length, c1, c2, c3, c4);

		poseStack.popPose();
	}

	private static void streak(GuiGraphics graphics, int width, int height, Color c1, Color c2, Color c3, Color c4) {
		if (NavigatableSimiScreen.isCurrentlyRenderingPreviousScreen())
			return;

		double split1 = .5;
		double split2 = .75;
		graphics.fillGradient(-width, 0, width, (int) (split1 * height), c1.getRGB(), c2.getRGB());
		graphics.fillGradient(-width, (int) (split1 * height), width, (int) (split2 * height), c2.getRGB(), c3.getRGB());
		graphics.fillGradient(-width, (int) (split2 * height), width, height, c3.getRGB(), c4.getRGB());
	}

	/**
	 * @see #angledGradient(GuiGraphics, float, int, int, int, float, float, Color, Color)
	 */
	public static void angledGradient(GuiGraphics graphics, float angle, int x, int y, float breadth, float length, Couple<Color> c) {
		angledGradient(graphics, angle, x, y, 0, breadth, length, c);
	}

	/**
	 * @see #angledGradient(GuiGraphics, float, int, int, int, float, float, Color, Color)
	 */
	public static void angledGradient(GuiGraphics graphics, float angle, int x, int y, int z, float breadth, float length, Couple<Color> c) {
		angledGradient(graphics, angle, x, y, z, breadth, length, c.getFirst(), c.getSecond());
	}

	/**
	 * @see #angledGradient(GuiGraphics, float, int, int, int, float, float, Color, Color)
	 */
	public static void angledGradient(GuiGraphics graphics, float angle, int x, int y, float breadth, float length, Color color1, Color color2) {
		angledGradient(graphics, angle, x, y, 0, breadth, length, color1, color2);
	}

	/**
	 * x and y specify the middle point of the starting edge
	 *
	 * @param angle   the angle of the gradient in degrees; 0° means from left to right
	 * @param startColor  the color at the starting edge
	 * @param endColor  the color at the ending edge
	 * @param breadth the total width of the gradient
	 */
	public static void angledGradient(GuiGraphics graphics, float angle, int x, int y, int z, float breadth, float length, Color startColor, Color endColor) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.mulPose(Axis.ZP.rotationDegrees(angle - 90));

		float w = breadth / 2;
		//graphics.fillGradient(-w, 0, w, length, startColor.getRGB(), endColor.getRGB());
		drawGradientRect(poseStack.last().pose(), 0, -w, 0f, w, length, startColor, endColor);

		poseStack.popPose();
	}

	public static void drawGradientRect(Matrix4f mat, int zLevel, float left, float top, float right, float bottom, Color startColor, Color endColor) {
		RenderSystem.enableDepthTest();
		//RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(mat, right,    top, zLevel).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha()).endVertex();
		buffer.vertex(mat,  left,    top, zLevel).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha()).endVertex();
		buffer.vertex(mat,  left, bottom, zLevel).color(  endColor.getRed(),   endColor.getGreen(),   endColor.getBlue(),   endColor.getAlpha()).endVertex();
		buffer.vertex(mat, right, bottom, zLevel).color(  endColor.getRed(),   endColor.getGreen(),   endColor.getBlue(),   endColor.getAlpha()).endVertex();
		tesselator.end();

		RenderSystem.disableBlend();
		//RenderSystem.enableTexture();
	}

	public static void breadcrumbArrow(GuiGraphics graphics, int x, int y, int z, int width, int height, int indent, Couple<Color> colors) {breadcrumbArrow(graphics, x, y, z, width, height, indent, colors.getFirst(), colors.getSecond());}

	// draws a wide chevron-style breadcrumb arrow pointing left
	public static void breadcrumbArrow(GuiGraphics graphics, int x, int y, int z, int width, int height, int indent, Color startColor, Color endColor) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x - indent, y, z);

		breadcrumbArrow(graphics, width, height, indent, startColor, endColor);

		poseStack.popPose();
	}

	private static void breadcrumbArrow(GuiGraphics graphics, int width, int height, int indent, Color c1, Color c2) {

		/*
		 * 0,0       x1,y0 ********************* x2,y0 ***** x3,y0
		 *       ****                                     ****
		 *   ****                                     ****
		 * x0,y1     x1,y1                       x2,y1
		 *   ****                                     ****
		 *       ****                                     ****
		 *           x1,y2 ********************* x2,y2 ***** x3,y2
		 *
		 */

		float x0 = 0;
		float x1 = indent;
		float x2 = width;
		float x3 = indent + width;

		float y0 = 0;
		float y1 = height / 2f;
		float y2 = height;

		indent = Math.abs(indent);
		width = Math.abs(width);
		Color fc1 = Color.mixColors(c1, c2, 0);
		Color fc2 = Color.mixColors(c1, c2, (indent) / (width + 2f * indent));
		Color fc3 = Color.mixColors(c1, c2, (indent + width) / (width + 2f * indent));
		Color fc4 = Color.mixColors(c1, c2, 1);

		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		Matrix4f model = graphics.pose().last().pose();
		bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

		bufferbuilder.vertex(model, x0, y1, 0).color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), fc1.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y0, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y1, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x0, y1, 0).color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), fc1.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y1, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y2, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x1, y2, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y0, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y0, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x1, y2, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y0, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y2, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x2, y1, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y0, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x3, y0, 0).color(fc4.getRed(), fc4.getGreen(), fc4.getBlue(), fc4.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x2, y2, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y1, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x3, y2, 0).color(fc4.getRed(), fc4.getGreen(), fc4.getBlue(), fc4.getAlpha()).endVertex();

		tessellator.end();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		//RenderSystem.enableTexture();
	}

	/**
	 * centered on 0, 0
	 *
	 * @param arcAngle length of the sector arc
	 */
	public static void drawRadialSector(GuiGraphics graphics, float innerRadius, float outerRadius, float startAngle, float arcAngle, Color innerColor, Color outerColor) {
		List<Point2D> innerPoints = getPointsForCircleArc(innerRadius, startAngle, arcAngle);
		List<Point2D> outerPoints = getPointsForCircleArc(outerRadius, startAngle, arcAngle);

		// if arcAngle > 0, start with inner. otherwise start with outer

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		BufferBuilder builder = Tesselator.getInstance().getBuilder();
		builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

		Matrix4f pose = graphics.pose().last().pose();
		Matrix3f n = graphics.pose().last().normal();

		for (int i = 0; i < innerPoints.size(); i++) {
			Point2D point = outerPoints.get(i);
			//builder.vertex(pose, (float) point.getX(), (float) point.getY(), 0).color(innerColor.getRGB()).normal(n, 1, 1, 0).endVertex();
			builder.vertex(pose, (float) point.getX(), (float) point.getY(), 0).color(outerColor.getRGB()).endVertex();

			point = innerPoints.get(i);
			builder.vertex(pose, (float) point.getX(), (float) point.getY(), 0).color(innerColor.getRGB()).endVertex();
		}

		Tesselator.getInstance().end();

		RenderSystem.disableBlend();

	}

	private static List<Point2D> getPointsForCircleArc(float radius, float startAngle, float arcAngle) {
		int segmentCount = Math.abs(arcAngle) <= 90 ? 16 : 32;
		List<Point2D> points = new ArrayList<>(segmentCount);


		float theta = (Mth.DEG_TO_RAD * arcAngle) / (float) (segmentCount - 1);
		float t = Mth.DEG_TO_RAD * startAngle;

		for (int i = 0; i < segmentCount; i++) {
			points.add(new Point2D.Float(
					(float) (radius * Math.cos(t)),
					(float) (radius * Math.sin(t))
			));

			t += theta;
		}

		return points;
	}


	//just like AbstractGui#drawTexture, but with a color at every vertex
	public static void drawColoredTexture(GuiGraphics graphics, Color c, int x, int y, int tex_left, int tex_top, int width, int height) {
		drawColoredTexture(graphics, c, x, y, 0, (float) tex_left, (float) tex_top, width, height, 256, 256);
	}

	public static void drawColoredTexture(GuiGraphics graphics, Color c, int x, int y, int z, float tex_left, float tex_top, int width, int height, int sheet_width, int sheet_height) {
		drawColoredTexture(graphics, c, x, x + width, y, y + height, z, width, height, tex_left, tex_top, sheet_width, sheet_height);
	}

	public static void drawStretched(GuiGraphics graphics, int left, int top, int w, int h, int z, TextureSheetSegment tex) {
		tex.bind();
		drawTexturedQuad(graphics.pose().last()
						.pose(), Color.WHITE, left, left + w, top, top + h, z, tex.getStartX() / 256f, (tex.getStartX() + tex.getWidth()) / 256f,
				tex.getStartY() / 256f, (tex.getStartY() + tex.getHeight()) / 256f);
	}

	public static void drawCropped(GuiGraphics graphics, int left, int top, int w, int h, int z, TextureSheetSegment tex) {
		tex.bind();
		drawTexturedQuad(graphics.pose().last()
						.pose(), Color.WHITE, left, left + w, top, top + h, z, tex.getStartX() / 256f, (tex.getStartX() + w) / 256f,
				tex.getStartY() / 256f, (tex.getStartY() + h) / 256f);
	}

	private static void drawColoredTexture(GuiGraphics graphics, Color c, int left, int right, int top, int bot, int z, int tex_width, int tex_height, float tex_left, float tex_top, int sheet_width, int sheet_height) {
		drawTexturedQuad(graphics.pose().last().pose(), c, left, right, top, bot, z, (tex_left + 0.0F) / (float) sheet_width, (tex_left + (float) tex_width) / (float) sheet_width, (tex_top + 0.0F) / (float) sheet_height, (tex_top + (float) tex_height) / (float) sheet_height);
	}

	private static void drawTexturedQuad(Matrix4f m, Color c, int left, int right, int top, int bot, int z, float u1, float u2, float v1, float v2) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferbuilder.vertex(m, (float) left , (float) bot, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u1, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) bot, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u2, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) top, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u2, v1).endVertex();
		bufferbuilder.vertex(m, (float) left , (float) top, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u1, v1).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
	}

	public static void flipForGuiRender(PoseStack poseStack) {
		poseStack.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
	}

	public static class CustomRenderTarget extends RenderTarget {

		public CustomRenderTarget(boolean useDepth) {
			super(useDepth);
		}

		public static CustomRenderTarget create(Window mainWindow) {
			CustomRenderTarget framebuffer = new CustomRenderTarget(true);
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
			framebuffer.setClearColor(0, 0, 0, 0);
			CatnipClientServices.CLIENT_HOOKS.enableStencilBuffer(framebuffer);
			return framebuffer;
		}

		public void renderWithAlpha(PoseStack poseStack, float alpha) {
			Window window = Minecraft.getInstance().getWindow();

			float guiScaledWidth = window.getGuiScaledWidth();
			float guiScaledHeight = window.getGuiScaledHeight();

			float vx = guiScaledWidth;
			float vy = guiScaledHeight;
			float tx = (float) viewWidth / (float) width;
			float ty = (float) viewHeight / (float) height;

			RenderSystem.disableDepthTest();

			Minecraft minecraft = Minecraft.getInstance();
			ShaderInstance shaderinstance = minecraft.gameRenderer.blitShader;
			shaderinstance.setSampler("DiffuseSampler", colorTextureId);
			//Matrix4f matrix4f = Matrix4f.orthographic(guiScaledWidth, -guiScaledHeight, 1000.0F, 3000.0F);
			Matrix4f matrix4f = poseStack.last().pose();
			Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
			RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
			if (shaderinstance.MODEL_VIEW_MATRIX != null) {
				shaderinstance.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
			}

			if (shaderinstance.PROJECTION_MATRIX != null) {
				shaderinstance.PROJECTION_MATRIX.set(matrix4f);
			}

			shaderinstance.apply();

			//bindRead();

			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			bufferbuilder.vertex(0 , vy, 0).uv(0 , 0 ).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex(vx, vy, 0).uv(tx, 0 ).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex(vx, 0 , 0).uv(tx, ty).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex(0 , 0 , 0).uv(0 , ty).color(1, 1, 1, alpha).endVertex();

			BufferUploader.draw(bufferbuilder.end());

			shaderinstance.clear();
			RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);
			//unbindRead();
		}

	}

}
