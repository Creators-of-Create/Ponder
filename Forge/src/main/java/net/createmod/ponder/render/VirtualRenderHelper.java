package net.createmod.ponder.render;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class VirtualRenderHelper {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();

	private static final RendererReloadCache<BlockState, Model> VIRTUAL_BLOCKS = new RendererReloadCache<>(state -> new ForgeBakedModelBuilder(Minecraft.getInstance().getBlockRenderer().getBlockModel(state)).modelData(VIRTUAL_DATA).build());

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

}
