package net.createmod.catnip.net.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.createmod.catnip.api.platform.Env;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.createmod.catnip.api.network.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class CatnipPacketRegistry {
	private final List<PacketType<? extends BasePacketPayload>> packets = new ArrayList<>();

	public CatnipPacketRegistry(String namespace, String version) {
	}

	public <T extends BasePacketPayload> void registerPacket(PacketType<T> packet) {
		packets.add(packet);
	}

	public void registerAllPackets() {
		for (PacketType<? extends BasePacketPayload> packet : packets) {
			register(packet);
		}
	}

	private <T extends BasePacketPayload> void register(PacketType<T> packet) {
		if (ServerboundPacketPayload.class.isAssignableFrom(packet.clazz())) {
			NetworkHelper.INSTANCE.serverboundCodecs().register(packet.type(), packet.codec());
			NetworkHelper.INSTANCE.registerPayloadHandler(packet.type(),
				(payload, player) -> ((ServerboundPacketPayload) payload).handle(player));
			return;
		}

		NetworkHelper.INSTANCE.clientboundCodecs().register(packet.type(), packet.codec());
		if (PlatformHelper.INSTANCE.getEnv() == Env.CLIENT) {
			registerClientHandler(packet);
		}
	}

	private static void registerClientHandler(PacketType<?> packet) {
		try {
			Class<?> bridgeClass = Class.forName("net.createmod.catnip.net.base.ClientboundPacketRegistryBridge");
			Method register = bridgeClass.getDeclaredMethod("register", PacketType.class);
			register.setAccessible(true);
			register.invoke(null, packet);
		} catch (ReflectiveOperationException | LinkageError ignored) {
		}
	}

	public record PacketType<T extends BasePacketPayload>(
		CustomPacketPayload.Type<T> type,
		Class<T> clazz,
		StreamCodec<? super RegistryFriendlyByteBuf, T> codec
	) {
	}
}
