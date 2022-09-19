package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.createmod.catnip.utility.theme.Color;
import net.minecraft.core.Direction;

public interface SuperByteBuffer {

	void renderInto(PoseStack ms, VertexConsumer consumer);

	boolean isEmpty();

	PoseStack getTransforms();

	<Self extends SuperByteBuffer> Self reset();

	<Self extends SuperByteBuffer> Self translate(float x, float y, float z);

	<Self extends SuperByteBuffer> Self rotate(Direction axis, float radians);

	<Self extends SuperByteBuffer> Self scale(float factorX, float factorY, float factorZ);

	<Self extends SuperByteBuffer> Self transform(PoseStack ms);

	<Self extends SuperByteBuffer> Self light(int packedLight);

	<Self extends SuperByteBuffer> Self light(Matrix4f lightTransform);

	<Self extends SuperByteBuffer> Self color(int color);

	<Self extends SuperByteBuffer> Self color(int r, int g, int b, int a);

	<Self extends SuperByteBuffer> Self shiftUV(SpriteShiftEntry entry);

	<Self extends SuperByteBuffer> Self shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV);

	<Self extends SuperByteBuffer> Self shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize);

	//

	default <Self extends SuperByteBuffer> Self translate(double x, double y, double z) {
		return this.translate((float) x, (float) y, (float) z);
	}

	default <Self extends SuperByteBuffer> Self rotate(Direction.Axis axis, float radians) {
		return rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), radians);
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(Direction axis, float radians) {
		return this
				.translate(.5f, .5f, .5f)
				.rotate(axis, radians)
				.translate(-.5f, -.5f, -.5f);
	}

	default <Self extends SuperByteBuffer> Self rotateCentered(Direction.Axis axis, float radians) {
		return rotateCentered(Direction.get(Direction.AxisDirection.POSITIVE, axis), radians);
	}

	default <Self extends SuperByteBuffer> Self scale(float factor) {
		return this.scale(factor, factor, factor);
	}

	default <Self extends SuperByteBuffer> Self light(Matrix4f lightTransform, int packedLight) {
		return this
				.light(lightTransform)
				.light(packedLight);
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

	@FunctionalInterface
	interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}
}
