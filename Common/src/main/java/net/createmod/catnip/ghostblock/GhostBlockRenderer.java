package net.createmod.catnip.ghostblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.impl.client.render.ColoringVertexConsumer;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class GhostBlockRenderer {
	private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();
	private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

	public static GhostBlockRenderer standard() {
		return STANDARD;
	}

	public static GhostBlockRenderer transparent() {
		return TRANSPARENT;
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params);

	private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {
		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params) {
			BlockState state = params.state;
			BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
			BlockPos pos = params.pos;

			ms.pushPose();
			ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			BakedModelBufferer.bufferModel(model, pos, EmptyVirtualBlockGetter.FULL_BRIGHT, state, ms, (layer, shade) -> buffer.getEarlyBuffer(layer));
			ms.popPose();
		}
	}

	private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {
		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params) {
			BlockState state = params.state;
			BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
			BlockPos pos = params.pos;
			float alpha = params.alphaSupplier.get() * .75f * PlacementClient.getCurrentAlpha();
			VertexConsumer vb = new ColoringVertexConsumer(buffer.getEarlyBuffer(RenderType.translucent()), 1, 1, 1, alpha);

			ms.pushPose();
			ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

			ms.translate(.5, .5, .5);
			ms.scale(.85f, .85f, .85f);
			ms.translate(-.5, -.5, -.5);
			BakedModelBufferer.bufferModel(model, pos, EmptyVirtualBlockGetter.FULL_BRIGHT, state, ms, (layer, shade) -> vb);
			ms.popPose();
		}
	}
}
