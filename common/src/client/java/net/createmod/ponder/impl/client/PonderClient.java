package net.createmod.ponder.impl.client;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.catnip.api.client.placement.PlacementAssistConfig;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.catnip.impl.network.ClientboundSimpleActionPacket;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.element.WorldSectionElementImpl;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.createmod.ponder.impl.client.plugin.BasePonderPlugin;
import net.createmod.ponder.impl.client.plugin.DebugPonderPlugin;
import net.createmod.ponder.impl.client.tooltip.PonderTooltipHandler;
import net.createmod.ponder.impl.config.PonderConfig;

public class PonderClient {
	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(WorldSectionElementImpl.PONDER_WORLD_SECTION);

		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);
		ClientboundSimpleActionPacket.addAction("reloadPonder", () -> SimplePonderActions::reloadPonder);

		ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(PonderSceneRenderState.class, PonderSceneRenderer::new);
		PlacementAssistConfig.setProvider(new PlacementAssistConfig.Provider() {
			@Override
			public PlacementAssistConfig.IndicatorSetting placementIndicator() {
				return PlacementAssistConfig.IndicatorSetting.valueOf(PonderConfig.client().placementIndicator.get().name());
			}

			@Override
			public float indicatorScale() {
				return PonderConfig.client().indicatorScale.getF();
			}
		});

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
