package net.createmod.catnip.render;

import com.jozufozu.flywheel.util.transform.TStack;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

@SuppressWarnings({"UnusedReturnValue", "unused", "unchecked"})
public interface SuperByteBuffer extends Transform<SuperByteBuffer>, TStack<SuperByteBuffer> {

	static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.block(packedLight1);
		int skyLight1 = LightTexture.sky(packedLight1);
		int blockLight2 = LightTexture.block(packedLight2);
		int skyLight2 = LightTexture.sky(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
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

	<Self extends SuperByteBuffer> Self overlay();

	<Self extends SuperByteBuffer> Self overlay(int overlay);

	/**
	 * Indicate that this buffer should look up the light coordinates in the current level.
	 */
	<Self extends SuperByteBuffer> Self light();

	/**
	 * Indicate that this buffer should look up the light coordinates in the current level.
	 * Light Positions will be transformed by the passed Matrix before the lookup.
	 */
	<Self extends SuperByteBuffer> Self light(Matrix4f lightTransform);

	<Self extends SuperByteBuffer> Self light(int packedLight);

	/**
	 * Use max light from calculated light (world light or custom light) and vertex
	 * light for the final light value. Ineffective if no other light method was called.
	 */
	<Self extends SuperByteBuffer> Self hybridLight();

	/**
	 * Transforms normals not only by the local matrix stack, but also by the passed
	 * matrix stack.
	 */
	<Self extends SuperByteBuffer> Self fullNormalTransform();

	//

	default void delete() {}

	default <Self extends SuperByteBuffer> Self rotate(Direction.Axis axis, float radians) {
		return (Self) rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), radians);
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

	default <Self extends SuperByteBuffer> Self forEntityRender() {
		return this
				.disableDiffuse()
				.overlay()
				.fullNormalTransform();
	}

	@FunctionalInterface
	interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}
}
