package net.createmod.ponder.impl.client;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.element.WorldSectionElementImpl;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.createmod.ponder.impl.client.plugin.BasePonderPlugin;
import net.createmod.ponder.impl.client.plugin.DebugPonderPlugin;
import net.createmod.ponder.impl.client.tooltip.PonderTooltipHandler;

public class PonderClient {
	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(WorldSectionElementImpl.PONDER_WORLD_SECTION);
		PonderClientConfig.register();

		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);
		ClientboundSimpleActionPacket.addAction("reloadPonder", () -> SimplePonderActions::reloadPonder);

		ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(PonderSceneRenderState.class, PonderSceneRenderer::new);

		PonderTooltipHandler.init();

		PonderIndex.addPlugin(new BasePonderPlugin());

		if (PlatformHelper.INSTANCE.isDevelopmentEnvironment()) {
			PonderIndex.addPlugin(new DebugPonderPlugin());
		}
	}

	public static void modLoadCompleted() {
		PonderIndex.registerAll();
	}
}
