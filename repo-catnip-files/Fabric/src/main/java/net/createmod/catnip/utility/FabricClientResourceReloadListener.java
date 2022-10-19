package net.createmod.catnip.utility;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.event.ClientResourceReloadListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricClientResourceReloadListener extends ClientResourceReloadListener implements IdentifiableResourceReloadListener {
	@Override
	public ResourceLocation getFabricId() {
		return Catnip.asResource("client_resource_reloader");
	}
}
