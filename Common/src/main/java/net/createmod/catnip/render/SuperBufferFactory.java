package net.createmod.catnip.render;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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

	public SuperByteBuffer create(MeshData data) {
		return new DefaultSuperByteBuffer(data);
	}

	public SuperByteBuffer createForBlock(BlockState renderedState) {
		return createForBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(renderedState), renderedState);
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState) {
		return createForBlock(model, referenceState, new PoseStack());
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState state, @Nullable PoseStack poseStack) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

		poseStack.pushPose();
		CatnipClientServices.CLIENT_HOOKS.tesselateBlockVirtual(dispatcher, model, state, BlockPos.ZERO, poseStack, sbbBuilder, false, random, 42L, OverlayTexture.NO_OVERLAY, null);
		poseStack.popPose();

		return sbbBuilder.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
	}
}
