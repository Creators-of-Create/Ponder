package net.createmod.catnip.impl.neoforge.render;

import java.util.List;
import java.util.function.ToIntFunction;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class VirtualRenderHelper {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();

	private static final RendererReloadCache<BlockState, Model> VIRTUAL_BLOCKS = new RendererReloadCache<>(state -> new BlockModelBuilder(new VirtualBlockGetter(state), List.of(BlockPos.ZERO)).build());

	public static boolean isVirtual(ModelData data) {
		return data.has(VirtualRenderHelper.VIRTUAL_PROPERTY) && Boolean.TRUE.equals(data.get(VirtualRenderHelper.VIRTUAL_PROPERTY));
	}

	/**
	 * A copy of {@link dev.engine_room.flywheel.lib.model.Models#block(BlockState)}, but with virtual model data passed in.
	 * @param state The block state to get the model for.
	 * @return The model for the given block state.
	 */
	public static Model blockModel(BlockState state) {
		return VIRTUAL_BLOCKS.get(state);
	}

	private static class VirtualBlockGetter extends SinglePosVirtualBlockGetter {
		private static final ToIntFunction<BlockPos> DARK_LIGHT = pos -> 0;

		VirtualBlockGetter(BlockState state) {
			super(DARK_LIGHT, DARK_LIGHT);
			blockState(state);
		}

		public ModelData getModelData(BlockPos pos) {
			return BlockPos.ZERO.equals(pos) ? VIRTUAL_DATA : ModelData.EMPTY;
		}
	}
}
