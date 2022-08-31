package net.createmod.catnip;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.event.ClientResourceReloadListener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.render.DefaultSuperRenderTypeBufferImpl;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.ghostblock.GhostBlocks;
import net.createmod.catnip.utility.outliner.Outliner;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class CatnipClient {

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();
	public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();
	public static final Outliner OUTLINER = new Outliner();

	public static void init() {
		UIRenderHelper.init();
	}

	public static void onTick() {
		if (!isGameActive())
			return;

		PlacementClient.tick();

		GHOST_BLOCKS.tickGhosts();
		OUTLINER.tickOutlines();

	}

	public static void onRenderWorld(PoseStack ms) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float partialTicks = AnimationTickHolder.getPartialTicks();

		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBufferImpl.getInstance();

		GHOST_BLOCKS.renderAll(ms, buffer);
		OUTLINER.renderOutlines(ms, buffer, partialTicks);

		buffer.draw();
		ms.popPose();

	}

	public static boolean isGameActive() {
		return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
	}

}
