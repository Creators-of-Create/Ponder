package net.createmod.ponder.utility;

import net.createmod.catnip.event.ClientResourceReloadListener;
import net.createmod.ponder.Ponder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricClientResourceReloadListener extends ClientResourceReloadListener implements IdentifiableResourceReloadListener {
	@Override
	public ResourceLocation getFabricId() {
		return Ponder.asResource("client_resource_reloader");
	}
}
