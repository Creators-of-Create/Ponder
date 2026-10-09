package net.createmod.catnip.api.client.render;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.api.client.render.SuperByteBufferCache.Compartment;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CachedBuffers {
	public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();
	public static final Compartment<BlockStateModel> BLOCK_STATE_MODEL = new Compartment<>();

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

	/**
	 * Creates and caches a SuperByteBuffer that has a BlockStateModel baked into it
	 *
	 * @param toRender the BlockStateModel to be rendered
	 * @param referenceState the BlockState associated with this model
	 * @return the cached SuperByteBuffer
	 */
	public static SuperByteBuffer blockStateModel(BlockStateModel toRender, BlockState referenceState) {
		return blockStateModel(BLOCK_STATE_MODEL, toRender, referenceState);
	}

	/**
	 * Creates a SuperByteBuffer that has a BlockStateModel baked into it <br />
	 * and caches it in the given Compartment
	 *
	 * @param compartment the Compartment the Buffer should be cached in
	 * @param toRender    the BlockStateModel to be rendered
	 * @param referenceState the BlockState associated with this model
	 * @return the cached SuperByteBuffer
	 */
	public static SuperByteBuffer blockStateModel(Compartment<BlockStateModel> compartment, BlockStateModel toRender, BlockState referenceState) {
		return SuperByteBufferCache.getInstance().get(compartment, toRender, () -> SuperBufferFactory.getInstance().createForBlock(toRender, referenceState));
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
}
