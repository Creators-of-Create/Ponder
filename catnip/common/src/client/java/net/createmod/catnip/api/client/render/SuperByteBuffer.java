package net.createmod.catnip.api.client.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;

import org.joml.Matrix4f;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public interface SuperByteBuffer {
	static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightCoordsUtil.block(packedLight1);
		int skyLight1 = LightCoordsUtil.sky(packedLight1);
		int blockLight2 = LightCoordsUtil.block(packedLight2);
		int skyLight2 = LightCoordsUtil.sky(packedLight2);
		return LightCoordsUtil.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	void renderInto(PoseStack ms, VertexConsumer consumer);

	boolean isEmpty();

	PoseStack getTransforms();

	<Self extends SuperByteBuffer> Self reset();

	<Self extends SuperByteBuffer> Self color(int color);

	<Self extends SuperByteBuffer> Self color(int r, int g, int b, int a);

	<Self extends SuperByteBuffer> Self disableDiffuse();

	<Self extends SuperByteBuffer> Self shiftUV(SpriteShiftEntry entry);

	<Self extends SuperByteBuffer> Self shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV);

	<Self extends SuperByteBuffer> Self shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize);

	<Self extends SuperByteBuffer> Self overlay(int overlay);

	<Self extends SuperByteBuffer> Self light(int packedLight);

	/**
	 * Indicate that this buffer should look up the light coordinates in the level.
	 */
	<Self extends SuperByteBuffer> Self useLevelLight(BlockAndTintGetter level);

	/**
	 * Indicate that this buffer should look up the light coordinates in the level.
	 * Light Positions will be transformed by the passed Matrix before the lookup.
	 */
	<Self extends SuperByteBuffer> Self useLevelLight(BlockAndTintGetter level, Matrix4f lightTransform);

	//

	default <Self extends SuperByteBuffer> Self useLevelLight(Level level) {
		return self();
	}

	default <Self extends SuperByteBuffer> Self useLevelLight(Level level, Matrix4f lightTransform) {
		return self();
	}

	default void delete() {
	}

	default <Self extends SuperByteBuffer> Self color(Color color) {
		return this.color(
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			color.getAlpha()
		);
	}

	default <Self extends SuperByteBuffer> Self shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		return this.shiftUVScrolling(entry, 0, scrollV);
	}

	@SuppressWarnings("unchecked")
	private <Self extends SuperByteBuffer> Self self() {
		return (Self) this;
	}

	default <Self extends SuperByteBuffer> Self transform(PoseStack poseStack) {
		getTransforms().mulPose(poseStack.last().pose());
		return self();
	}

	default <Self extends SuperByteBuffer> Self mulPose(Matrix4fc pose) {
		getTransforms().mulPose(pose);
		return self();
	}

	default <Self extends SuperByteBuffer> Self mulNormal(Matrix3fc normal) {
		return self();
	}

	default <Self extends SuperByteBuffer> Self translate(Vec3 vec) {
		getTransforms().translate(vec);
		return self();
	}

	default <Self extends SuperByteBuffer> Self translate(double x, double y, double z) {
		getTransforms().translate(x, y, z);
		return self();
	}

	default <Self extends SuperByteBuffer> Self translateBack(double x, double y, double z) {
		return translate(-x, -y, -z);
	}

	default <Self extends SuperByteBuffer> Self translateBack(float x, float y, float z) {
		return translate(-x, -y, -z);
	}

	default <Self extends SuperByteBuffer> Self translateBack(Vec3 vec) {
		return translate(-vec.x, -vec.y, -vec.z);
	}

	default <Self extends SuperByteBuffer> Self nudge(int seed) {
		double nudge = ((seed * 31L) & 0xFFFF) / 65535.0 * 1e-4;
		return translate(nudge, nudge, nudge);
	}

	default <Self extends SuperByteBuffer> Self nudge(double x, double y, double z) {
		return translate(x, y, z);
	}

	default <Self extends SuperByteBuffer> Self scale(float scale) {
		return scale(scale, scale, scale);
	}

	default <Self extends SuperByteBuffer> Self scale(float x, float y, float z) {
		getTransforms().scale(x, y, z);
		return self();
	}

	default <Self extends SuperByteBuffer> Self center() {
		return translate(.5, .5, .5);
	}

	default <Self extends SuperByteBuffer> Self uncenter() {
		return translate(-.5, -.5, -.5);
	}

	default <Self extends SuperByteBuffer> Self rotateXDegrees(float degrees) {
		getTransforms().mulPose(Axis.XP.rotationDegrees(degrees));
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateYDegrees(float degrees) {
		getTransforms().mulPose(Axis.YP.rotationDegrees(degrees));
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateZDegrees(float degrees) {
		getTransforms().mulPose(Axis.ZP.rotationDegrees(degrees));
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotate(float radians, Direction axis) {
		getTransforms().mulPose(axisFor(axis).rotation(radians * axis.getAxisDirection()
			.getStep()));
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotate(double radians, Direction axis) {
		return rotate((float) radians, axis);
	}

	default <Self extends SuperByteBuffer> Self rotate(Quaternionfc rotation) {
		getTransforms().mulPose(rotation);
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(float radians, Direction axis) {
		center();
		rotate(radians, axis);
		uncenter();
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(float radians, Axis axis) {
		center();
		getTransforms().mulPose(axis.rotation(radians));
		uncenter();
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(double radians, Direction axis) {
		return rotateCentered((float) radians, axis);
	}

	default <Self extends SuperByteBuffer> Self rotateYCenteredDegrees(float degrees) {
		return rotateCentered(com.mojang.math.Axis.YP.rotationDegrees(degrees));
	}

	default <Self extends SuperByteBuffer> Self rotateXCenteredDegrees(float degrees) {
		return rotateCentered(com.mojang.math.Axis.XP.rotationDegrees(degrees));
	}

	default <Self extends SuperByteBuffer> Self rotateZCenteredDegrees(float degrees) {
		return rotateCentered(com.mojang.math.Axis.ZP.rotationDegrees(degrees));
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(Quaternionfc rotation) {
		center();
		rotate(rotation);
		uncenter();
		return self();
	}

	default <Self extends SuperByteBuffer> Self rotateToFace(Direction facing) {
		center();
		rotateYDegrees(AngleHelper.horizontalAngle(facing));
		rotateXDegrees(AngleHelper.verticalAngle(facing));
		uncenter();
		return self();
	}

	private static Axis axisFor(Direction direction) {
		return switch (direction.getAxis()) {
			case X -> Axis.XP;
			case Y -> Axis.YP;
			case Z -> Axis.ZP;
		};
	}

	@FunctionalInterface
	interface SpriteShiftFunc {
		void shift(float u, float v, Output output);

		interface Output {
			void accept(float u, float v);
		}
	}

	class ShiftOutput implements SpriteShiftFunc.Output {
		public float u;
		public float v;

		@Override
		public void accept(float u, float v) {
			this.u = u;
			this.v = v;
		}
	}
}
