package net.createmod.catnip.render;

import net.createmod.catnip.render.SuperByteBufferCache.Compartment;
import net.minecraft.world.level.block.state.BlockState;

public class CachedBlockBuffers {

	public static final Compartment<BlockState> GENERIC_BLOCK = new Compartment<>();

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

}
