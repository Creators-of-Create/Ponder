package net.createmod.catnip.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.event.AtlasStitchedCallback;
import net.createmod.catnip.api.client.event.ClientTickCallback;
import net.createmod.catnip.api.client.event.LevelRenderCallback;
import net.createmod.catnip.api.client.event.LevelRendererReloadCallback;
import net.createmod.catnip.api.client.ghostblock.GhostBlocks;
import net.createmod.catnip.api.client.gui.HudElements;
import net.createmod.catnip.api.client.gui.render.pip.GuiBlockEntityRenderState;
import net.createmod.catnip.api.client.gui.render.pip.GuiBlockModelRenderState;
import net.createmod.catnip.api.client.gui.render.pip.GuiFluidStateRenderState;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.CatnipRenderPipelines;
import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.api.client.render.StitchedSprite;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.createmod.catnip.api.data.ReloadListenerRegistries;
import net.createmod.catnip.impl.client.gui.element.pip.GuiBlockEntityRenderer;
import net.createmod.catnip.impl.client.gui.element.pip.GuiBlockModelRenderer;
import net.createmod.catnip.impl.client.gui.element.pip.GuiFluidStateRenderer;
import net.createmod.catnip.impl.client.placement.PlacementClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;

public final class CatnipClient {
	public static void preInit() {
		CatnipRenderPipelines.init();
	}

	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.GENERIC_BLOCK);
	    CatnipClientPayloadHandlers.register();

		ClientTickCallback.EVENT.pre().subscribe(CatnipClient::beforeClientTick);
		LevelRendererReloadCallback.EVENT.subscribe(CatnipClient::onRendererReload);
		LevelRenderCallback.AFTER_TRANSLUCENT_FEATURES.subscribe(CatnipClient::onLevelRender);
		AtlasStitchedCallback.EVENT.subscribe(StitchedSprite::afterAtlasStitch);

		ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(GuiBlockModelRenderState.class, GuiBlockModelRenderer::new);
		ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(GuiBlockEntityRenderState.class, GuiBlockEntityRenderer::new);
		ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(GuiFluidStateRenderState.class, GuiFluidStateRenderer::new);

		ReloadListenerRegistries.INSTANCE.assets().register(CatnipReloadListener.ID, CatnipReloadListener.INSTANCE);
		HudElements.INSTANCE.register(Catnip.id("placement_helper"), PlacementClient::renderCrosshairOverlay);
	}

	private static void beforeClientTick() {
		AnimationTickHolder.tick();

		if (!isGameActive())
			return;

		PlacementClient.tick(); // Should be called before GhostBlocks' tick as it can add new ghosts

		GhostBlocks.getInstance().tickGhosts();
		Outliner.getInstance().tickOutlines();
	}

	private static void onRendererReload() {
		AnimationTickHolder.reset();
		SuperByteBufferCache.getInstance().invalidate();
	}

	public static void onLevelRender(LevelRenderer renderer, LevelRenderState state, PoseStack transforms) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
		float partialTicks = AnimationTickHolder.getPartialTicks();

		transforms.pushPose();
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();

		GhostBlocks.getInstance().renderAll(transforms, buffer, cameraPos);
		Outliner.getInstance().renderOutlines(transforms, buffer, cameraPos, partialTicks);

		buffer.draw();
		transforms.popPose();
	}

	public static boolean isGameActive() {
		return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
	}
}
