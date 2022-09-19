package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public interface SuperBufferFactory {

	static SuperBufferFactory getInstance() {
		return DefaultSuperBufferFactory.getInstance();
	}

	static void setInstance(SuperBufferFactory factory) {
		DefaultSuperBufferFactory.setInstance(factory);
	}

	SuperByteBuffer create(BufferBuilder builder);

	SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState, PoseStack ms);

	default SuperByteBuffer createForBlock(BlockState renderedState) {
		return createForBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(renderedState), renderedState);
	}

	default SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState) {
		return createForBlock(model, referenceState, new PoseStack());
	}

}
