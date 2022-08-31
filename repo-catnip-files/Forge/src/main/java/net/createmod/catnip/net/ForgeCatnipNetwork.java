package net.createmod.catnip.net;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.createmod.catnip.Catnip;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ForgeCatnipNetwork {

	private static int index = 0;

	public static final ResourceLocation CHANNEL_NAME = Catnip.asResource("main");
	public static final int NETWORK_VERSION = 1;
	public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(CHANNEL_NAME)
			.serverAcceptedVersions(NETWORK_VERSION_STR::equals)
			.clientAcceptedVersions(NETWORK_VERSION_STR::equals)
			.networkProtocolVersion(() -> NETWORK_VERSION_STR)
			.simpleChannel();


	public static void register() {
		registerServerbound(ServerboundConfigPacket.class, ServerboundConfigPacket::new);

		registerClientbound(ClientboundSimpleActionPacket.class, ClientboundSimpleActionPacket::new, ClientboundSimpleActionPacket.Handler::handle);
		registerClientbound(ClientboundConfigPacket.class, ClientboundConfigPacket::new, ClientboundConfigPacket.Handler::handle);
	}

	private static <T extends ClientboundPacket> void registerClientbound(Class<T> type, Function<FriendlyByteBuf, T> factory, Consumer<T> handler) {
		CHANNEL.messageBuilder(type, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(T::write)
				.decoder(factory)
				.consumer(clientHandler(handler))
				.add();
	}

	private static <T extends ClientboundPacket> BiConsumer<T, Supplier<NetworkEvent.Context>> clientHandler(Consumer<T> handler) {
		return (t, contextSupplier) -> {
			handler.accept(t);
			contextSupplier.get().setPacketHandled(true);
		};
	}

	private static <T extends ServerboundPacket> void registerServerbound(Class<T> type, Function<FriendlyByteBuf, T> factory) {
		CHANNEL.messageBuilder(type, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(T::write)
				.decoder(factory)
				.consumer(serverHandler())
				.add();
	}

	private static <T extends ServerboundPacket> BiConsumer<T, Supplier<NetworkEvent.Context>> serverHandler() {
		return (t, contextSupplier) -> {
			ServerPlayer sender = contextSupplier.get().getSender();
			t.handle(sender != null ? sender.getServer() : null, sender);
			contextSupplier.get().setPacketHandled(true);
		};
	}

}
