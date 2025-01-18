package net.createmod.catnip.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;

@SuppressWarnings({"UnusedReturnValue", "unused", "unchecked"})
public interface SuperByteBuffer extends TransformStack<SuperByteBuffer> {

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

	default void delete() {}

	default <Self extends SuperByteBuffer> Self rotate(Direction.Axis axis, float radians) {
		return (Self) rotate(radians, axis);
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
