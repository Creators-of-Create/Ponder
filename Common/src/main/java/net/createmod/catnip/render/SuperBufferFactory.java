package net.createmod.catnip.render;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SuperBufferFactory {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private static SuperBufferFactory instance = new SuperBufferFactory();

	public static SuperBufferFactory getInstance() {
		return instance;
	}

	static void setInstance(SuperBufferFactory factory) {
		instance = factory;
	}

	public SuperByteBuffer create(RenderedBuffer data) {
		return new ShadeSeparatingSuperByteBuffer(new MutableTemplateMesh(data).toImmutable());
	}

	public SuperByteBuffer createForBlock(BlockState renderedState) {
		return createForBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(renderedState), renderedState);
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState) {
		return createForBlock(model, referenceState, new PoseStack());
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState state, @Nullable PoseStack poseStack) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		SbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.prepare();
		BakedModelBufferer.bufferModel(model, BlockPos.ZERO, EmptyVirtualBlockGetter.FULL_DARK, state, poseStack, sbbBuilder);
		return sbbBuilder.build();
	}

	private static class SbbBuilder extends SuperByteBufferBuilder implements ShadeSeparatedResultConsumer {
		@Override
		public void accept(RenderType renderType, boolean shaded, RenderedBuffer data) {
			add(data, shaded);
		}
	}

	private static class ThreadLocalObjects {
		public final SbbBuilder sbbBuilder = new SbbBuilder();
	}
}
