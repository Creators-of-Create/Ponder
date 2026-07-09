package net.createmod.catnip.api.client.render;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.api.client.render.SuperByteBufferCache.Compartment;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CachedBuffers {
	public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();
	public static final Compartment<PartialKey> PARTIAL = new Compartment<>();
	public static final Compartment<DirectionalPartialKey> DIRECTIONAL_PARTIAL = new Compartment<>();

	/**
	 * Creates and caches a SuperByteBuffer that has the model of a BlockState baked into it
	 *
	 * @param toRender the BlockState to be rendered
	 * @return the cached SuperByteBuffer
	 */
	public static SuperByteBuffer block(BlockState toRender) {
		return block(GENERIC_BLOCK, toRender);
	}

	/**
	 * Creates a SuperByteBuffer that has the model of a BlockState baked into it <br />
	 * and caches it in the given Compartment
	 *
	 * @param compartment the Compartment the Buffer should be cached in
	 * @param toRender    the BlockState to be rendered
	 * @return the cached SuperByteBuffer
	 */
	public static SuperByteBuffer block(Compartment<BlockState> compartment, BlockState toRender) {
		return SuperByteBufferCache.getInstance().get(compartment, toRender, () -> SuperBufferFactory.getInstance().createForBlock(toRender));
	}

	public static SuperByteBuffer partial(Object partial, BlockState referenceState) {
		return SuperByteBufferCache.getInstance()
			.get(PARTIAL, new PartialKey(partial, referenceState), () -> SuperBufferFactory.getInstance()
				.createForBlock(referenceState));
	}

	public static SuperByteBuffer partialFacing(Object partial, BlockState referenceState) {
		return partialFacing(partial, referenceState, getFacing(referenceState));
	}

	public static SuperByteBuffer partialFacing(Object partial, BlockState referenceState, Direction facing) {
		return partialDirectional(partial, referenceState, facing, rotateToFace(facing));
	}

	public static SuperByteBuffer partialFacingVertical(Object partial, BlockState referenceState, Direction facing) {
		return partialDirectional(partial, referenceState, facing, rotateToFaceVertical(facing));
	}

	public static SuperByteBuffer partialDirectional(Object partial, BlockState referenceState, Direction direction,
		Supplier<PoseStack> transform) {
		return SuperByteBufferCache.getInstance()
			.get(DIRECTIONAL_PARTIAL, new DirectionalPartialKey(partial, referenceState, direction, transform),
				() -> SuperBufferFactory.getInstance()
					.createForBlock(referenceState)
					.transform(transform.get()));
	}

	private static Direction getFacing(BlockState state) {
		if (state.hasProperty(BlockStateProperties.FACING))
			return state.getValue(BlockStateProperties.FACING);
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		return Direction.NORTH;
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			stack.translate(0.5, 0.5, 0.5);
			stack.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
			stack.mulPose(Axis.XP.rotationDegrees(AngleHelper.verticalAngle(facing)));
			stack.translate(-0.5, -0.5, -0.5);
			return stack;
		};
	}

	public static Supplier<PoseStack> rotateToFaceVertical(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			stack.translate(0.5, 0.5, 0.5);
			stack.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
			stack.mulPose(Axis.XP.rotationDegrees(AngleHelper.verticalAngle(facing) + 90));
			stack.translate(-0.5, -0.5, -0.5);
			return stack;
		};
	}

	private record PartialKey(Object partial, BlockState referenceState) {}

	private record DirectionalPartialKey(Object partial, BlockState referenceState, Direction direction,
		Supplier<PoseStack> transform) {}
}
