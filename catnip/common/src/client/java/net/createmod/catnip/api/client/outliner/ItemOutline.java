package net.createmod.catnip.api.client.outliner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemOutline extends Outline {
	protected Vec3 pos;
	protected ItemStack stack;

	protected ItemStackRenderState renderState = new ItemStackRenderState();
	protected SubmitNodeStorage queue = new SubmitNodeStorage();
	//protected PoseStack poseStack;

	public ItemOutline(Vec3 pos, ItemStack stack) {
		this.pos = pos;
		this.stack = stack;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
		ms.pushPose();

		ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
		ms.scale(params.alpha, params.alpha, params.alpha);

		Minecraft.getInstance()
			.getItemModelResolver()
			.updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, null, null, 0);
		renderState.submit(ms, queue, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

		ms.popPose();
	}
}
