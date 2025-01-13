package net.createmod.ponder.net;

import java.util.function.Consumer;
import java.util.function.Function;

import net.createmod.catnip.net.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class FabricPonderNetwork {
	public static void register() {

		registerServerbound(ServerboundConfigPacket.ID, ServerboundConfigPacket::new);

		registerClientbound(ClientboundSimpleActionPacket.ID, ClientboundSimpleActionPacket::new, ClientboundSimpleActionPacket.Handler::handle);
		registerClientbound(ClientboundConfigPacket.ID, ClientboundConfigPacket::new, ClientboundConfigPacket.Handler::handle);
	}

	private static <T extends ClientboundPacket> void registerClientbound(ResourceLocation location, Function<FriendlyByteBuf, T> factory, Consumer<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(location, (client, handler1, buf, responseSender) -> {
			handler.accept(factory.apply(buf));
		});
	}

	private static <T extends ServerboundPacket> void registerServerbound(ResourceLocation location, Function<FriendlyByteBuf, T> factory) {
		ServerPlayNetworking.registerGlobalReceiver(location, (server, player, handler, buf, responseSender) -> {
			factory.apply(buf).handle(server, player);
		});
	}
}
