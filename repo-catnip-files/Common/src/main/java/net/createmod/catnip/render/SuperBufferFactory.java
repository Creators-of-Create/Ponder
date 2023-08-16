package net.createmod.catnip.render;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public class SuperBufferFactory {

	private static SuperBufferFactory instance = new SuperBufferFactory();

	public static SuperBufferFactory getInstance() {
		return instance;
	}

	static void setInstance(SuperBufferFactory factory) {
		instance = factory;
	}

	public SuperByteBuffer create(RenderedBuffer builder) {
		return new DefaultSuperByteBuffer(builder);
	}

	public SuperByteBuffer createForBlock(BlockState renderedState) {
		return createForBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(renderedState), renderedState);
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState) {
		return createForBlock(model, referenceState, new PoseStack());
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState, PoseStack ms) {
		ShadeSeparatedBufferedData data = ModelUtil.getBufferedData(model, referenceState, ms);
		ShadeSpearatingSuperByteBuffer sbb = new ShadeSpearatingSuperByteBuffer(data);
		data.release();
		return sbb;
	}

}
