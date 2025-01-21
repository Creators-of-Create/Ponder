package net.createmod.catnip.net.packets;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.net.CatnipPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.ponder.Ponder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerboundConfigPacket<T> implements ServerboundPacketPayload {
	@SuppressWarnings("rawtypes")
	public static final StreamCodec<ByteBuf, ServerboundConfigPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, p -> p.modID,
		ByteBufCodecs.STRING_UTF8, p -> p.path,
		ByteBufCodecs.STRING_UTF8, p -> p.value,
		ServerboundConfigPacket::new
	);

	private final String modID;
	private final String path;
	private final String value;

	public ServerboundConfigPacket(String modID, String path, T value) {
		super();
		this.modID = Objects.requireNonNull(modID);
		this.path = path;
		this.value = serialize(value);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CatnipPackets.SERVERBOUND_CONFIG;
	}

	@Override
	public void handle(ServerPlayer player) {
		try {
			if (!player.hasPermissions(2))
				return;

			ModConfigSpec spec = ConfigHelper.findModConfigSpecFor(ModConfig.Type.SERVER, modID);
			if (spec == null)
				return;
			ModConfigSpec.ValueSpec valueSpec = spec.getSpec().getRaw(path);
			ModConfigSpec.ConfigValue<T> configValue = spec.getValues().get(path);

			T v = (T) deserialize(configValue.get(), value);
			if (!valueSpec.test(v))
				return;

			configValue.set(v);
		} catch (Exception e) {
			Ponder.LOGGER.warn("Unable to handle ConfigureConfig Packet. ", e);
		}
	}

	public String serialize(T value) {
		if (value instanceof Boolean)
			return Boolean.toString((Boolean) value);
		if (value instanceof Enum<?>)
			return ((Enum<?>) value).name();
		if (value instanceof Integer)
			return Integer.toString((Integer) value);
		if (value instanceof Float)
			return Float.toString((Float) value);
		if (value instanceof Double)
			return Double.toString((Double) value);

		throw new IllegalArgumentException("unknown type " + value + ": " + value.getClass().getSimpleName());
	}

	public static Object deserialize(Object type, String sValue) {
		if (type instanceof Boolean)
			return Boolean.parseBoolean(sValue);
		if (type instanceof Enum<?>)
			return Enum.valueOf(((Enum<?>) type).getClass(), sValue);
		if (type instanceof Integer)
			return Integer.parseInt(sValue);
		if (type instanceof Float)
			return Float.parseFloat(sValue);
		if (type instanceof Double)
			return Double.parseDouble(sValue);

		throw new IllegalArgumentException("unknown type " + type + ": " + type.getClass().getSimpleName());
	}
}
