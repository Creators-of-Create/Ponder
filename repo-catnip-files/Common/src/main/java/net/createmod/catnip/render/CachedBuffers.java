package net.createmod.catnip.render;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.SuperByteBufferCache.Compartment;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CachedBuffers {

	public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();
	public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
	public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL = new Compartment<>();

	/**
	 * Creates and caches a SuperByteBuffer that has the model of a BlockState baked into it
	 *
	 * @param toRender the BlockState to be rendered
	 *
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
	 * @param toRender the BlockState to be rendered
	 *
	 * @return the cached SuperByteBuffer
	 */
	public static SuperByteBuffer block(Compartment<BlockState> compartment, BlockState toRender) {
		return SuperByteBufferCache.getInstance().get(compartment, toRender, () -> SuperBufferFactory.getInstance().createForBlock(toRender));
	}

	public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState) {
		return SuperByteBufferCache.getInstance().get(PARTIAL, partial,
				() -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState));
	}

	public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState,
										  Supplier<PoseStack> modelTransform) {
		return SuperByteBufferCache.getInstance().get(PARTIAL, partial,
				() -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState, modelTransform.get()));
	}

	public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState) {
		Direction facing = referenceState.getValue(FACING);
		return partialFacing(partial, referenceState, facing);
	}

	public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState, Direction facing) {
		return partialDirectional(partial, referenceState, facing,
			rotateToFace(facing));
	}

	public static SuperByteBuffer partialFacingVertical(PartialModel partial, BlockState referenceState, Direction facing) {
		return partialDirectional(partial, referenceState, facing,
			rotateToFaceVertical(facing));
	}

	public static SuperByteBuffer partialDirectional(PartialModel partial, BlockState referenceState, Direction dir,
													 Supplier<PoseStack> modelTransform) {
		return SuperByteBufferCache.getInstance().get(DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
				() -> SuperBufferFactory.getInstance().createForBlock(partial.get(), referenceState, modelTransform.get()));
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack.cast(stack)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing))
				.unCentre();
			return stack;
		};
	}

	public static Supplier<PoseStack> rotateToFaceVertical(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack.cast(stack)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing) + 90)
				.unCentre();
			return stack;
		};
	}
}
