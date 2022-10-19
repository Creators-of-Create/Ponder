package net.createmod.catnip;

import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback;
import net.createmod.catnip.event.ClientResourceReloadListener;
import net.createmod.catnip.render.SpriteShifter;
import net.createmod.catnip.utility.FabricClientResourceReloadListener;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.function.Consumer;

public class FabricCatnipClient implements ClientModInitializer {

	public static final FabricClientResourceReloadListener FABRIC$RESOURCE_RELOAD_LISTENER = new FabricClientResourceReloadListener();

	@Override
	public void onInitializeClient() {
		CatnipClient.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> CatnipClient.onTick());
		ClientWorldEvents.LOAD.register((client, world) -> CatnipClient.onLoadWorld(world));
		ClientWorldEvents.UNLOAD.register((client, world) -> CatnipClient.onUnloadWorld(world));
		OverlayRenderCallback.EVENT.register((stack, partialTicks, window, type) -> {
			if (type != OverlayRenderCallback.Types.CROSSHAIRS)
				return false;
			PlacementClient.onRenderCrosshairOverlay(window, stack, partialTicks);
			return false;
		});

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FABRIC$RESOURCE_RELOAD_LISTENER);
		TextureStitchCallback.PRE.register(FabricCatnipClient::onTextureStitchPre);
		TextureStitchCallback.POST.register(FabricCatnipClient::onTextureStitchPost);
	}

	public static void onTextureStitchPre(TextureAtlas atlas, Consumer<ResourceLocation> spriteAdder) {
		if (!atlas.location().equals(InventoryMenu.BLOCK_ATLAS))
			return;

		SpriteShifter.getAllTargetSprites().forEach(spriteAdder);
	}

	public static void onTextureStitchPost(TextureAtlas atlas) {
		if (!atlas.location().equals(InventoryMenu.BLOCK_ATLAS))
			return;

		SpriteShifter.getAllShifts().forEach(entry -> entry.loadTextures(atlas));
	}
}
