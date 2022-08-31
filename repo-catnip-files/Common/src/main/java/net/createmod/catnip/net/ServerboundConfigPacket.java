package net.createmod.catnip.net;

import java.util.Objects;

import javax.annotation.Nullable;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ServerboundConfigPacket<T> implements ServerboundPacket {

	private final String modID;
	private final String path;
	private final String value;

	public ServerboundConfigPacket(String modID, String path, T value) {
		super();
		this.modID = Objects.requireNonNull(modID);
		this.path = path;
		this.value = serialize(value);
	}

	public ServerboundConfigPacket(FriendlyByteBuf buffer) {
		this.modID = buffer.readUtf(32767);
		this.path = buffer.readUtf(32767);
		this.value = buffer.readUtf(32767);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(modID);
		buffer.writeUtf(path);
		buffer.writeUtf(value);
	}

	@Override
	public void handle(@Nullable MinecraftServer server, @Nullable ServerPlayer player) {
		if (server == null || player == null) {
			Catnip.LOGGER.error("Unable to handle ConfigureConfig Packet. Player ({}) or Server ({}) was null!", player, server);
			return;
		}
		server.execute(() -> {
			try {
				if (!player.hasPermissions(2))
					return;

				ForgeConfigSpec spec = ConfigHelper.findForgeConfigSpecFor(ModConfig.Type.SERVER, modID);
				if (spec == null)
					return;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(path);
				ForgeConfigSpec.ConfigValue<T> configValue = spec.getValues().get(path);

				T v = (T) deserialize(configValue.get(), value);
				if (!valueSpec.test(v))
					return;

				configValue.set(v);
			} catch (Exception e) {
				Catnip.LOGGER.warn("Unable to handle ConfigureConfig Packet. ", e);
			}
		});
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
