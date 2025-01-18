package net.createmod.ponder;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.event.ClientResourceReloadListener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.net.packets.ClientboundSimpleActionPacket;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.ghostblock.GhostBlocks;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.ponder.command.SimplePonderActions;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.content.BasePonderPlugin;
import net.createmod.ponder.foundation.content.DebugPonderPlugin;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class PonderClient {

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();
	public static final GhostBlocks GHOST_BLOCKS = GhostBlocks.getInstance();
	public static final boolean ENABLE_DEBUG = true;

	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.GENERIC_BLOCK);
		SuperByteBufferCache.getInstance().registerCompartment(WorldSectionElementImpl.PONDER_WORLD_SECTION);

		UIRenderHelper.init();

		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);
		ClientboundSimpleActionPacket.addAction("reloadPonder", () -> SimplePonderActions::reloadPonder);

		PonderIndex.addPlugin(new BasePonderPlugin());

		if (ENABLE_DEBUG) {
			PonderIndex.addPlugin(new DebugPonderPlugin());
		}

	}

	public static void modLoadCompleted() {
		PonderIndex.registerAll();
	}

	public static void onTick() {
		AnimationTickHolder.tick();

		if (!isGameActive())
			return;

		PlacementClient.tick(); // Should be called before GhostBlocks' tick as it can add new ghosts

		GhostBlocks.getInstance().tickGhosts();
		Outliner.getInstance().tickOutlines();

	}

	public static void onRenderWorld(PoseStack ms) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float partialTicks = AnimationTickHolder.getPartialTicks();

		ms.pushPose();
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();

		GHOST_BLOCKS.renderAll(ms, buffer, cameraPos);
		Outliner.getInstance().renderOutlines(ms, buffer, cameraPos, partialTicks);

		buffer.draw();
		ms.popPose();
	}

	public static void invalidateRenderers() {
		SuperByteBufferCache.getInstance().invalidate();
	}

	public static void onLoadWorld(LevelAccessor level) {
		if (!level.isClientSide())
			return;

		if (level instanceof ClientLevel && !(level instanceof WrappedClientLevel)) {
			invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(LevelAccessor level) {
		if (!level.isClientSide())
			return;

		invalidateRenderers();
		AnimationTickHolder.reset();
	}

	public static boolean isGameActive() {
		return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
	}

}
