package net.createmod.catnip.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.createmod.ponder.Ponder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSimpleActionPacket implements ClientboundPacket {

	public static final ResourceLocation ID = Ponder.asResource("simple_action_packet");

	private static final Map<String, Supplier<Consumer<String>>> actions = new HashMap<>();

	public static void addAction(String name, Supplier<Consumer<String>> action) {
		actions.put(name, action);
	}

	static {
		addAction("test", () -> System.out::println);
		addAction("configScreen", () -> SimpleCatnipActions::configScreen);
	}

	private final String action;
	private final String value;

	public ClientboundSimpleActionPacket(String action, String value) {
		this.action = action;
		this.value = value;
	}

	public ClientboundSimpleActionPacket(FriendlyByteBuf buffer) {
		this.action = buffer.readUtf();
		this.value = buffer.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(action);
		buffer.writeUtf(value);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public static class Handler {
		public static void handle(ClientboundSimpleActionPacket packet) {
			if (!actions.containsKey(packet.action)) {
				Ponder.LOGGER.warn("Received ClientboundSimpleActionPacket with invalid Action {}, ignoring the packet", packet.action);
				return;
			}

			Minecraft.getInstance().execute(() -> actions.get(packet.action).get().accept(packet.value));
		}
	}
}
