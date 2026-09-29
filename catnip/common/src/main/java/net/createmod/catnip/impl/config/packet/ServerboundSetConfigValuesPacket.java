package net.createmod.catnip.impl.config.packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.logging.LogUtils;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.api.network.SelfHandlingPayload;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.Bool;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.EnumOrdinal;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.Float64;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.Float32;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.Int;
import net.createmod.catnip.impl.config.packet.WrappedConfigValue.Str;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;

public record ServerboundSetConfigValuesPacket(String modId, Map<ConfigPath, WrappedConfigValue> changes) implements SelfHandlingPayload {
	public static final StreamCodec<ByteBuf, ServerboundSetConfigValuesPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ServerboundSetConfigValuesPacket::modId,
		ByteBufCodecs.map(HashMap::new, ConfigPath.STREAM_CODEC, WrappedConfigValue.STREAM_CODEC), ServerboundSetConfigValuesPacket::changes,
		ServerboundSetConfigValuesPacket::new
	);

	private static final Logger logger = LogUtils.getLogger();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return CatnipPayloads.SET_CONFIG_VALUES;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (!ConfigHelper.CAN_CHANGE_CONFIGS.check(player.permissions())) {
			logger.warn("Ignoring ServerboundSetConfigValuesPacket from unprivileged player {}", player.getName());
			return;
		}

		ConfigId id = new ConfigId(this.modId, ConfigSide.SERVER);
		ConfigAccess config = ConfigAccess.FIND.invoker().get(id);
		if (config == null) {
			logger.warn("Ignoring ServerboundSetConfigValuesPacket targeting unknown config {}", id);
			return;
		}

		this.changes.forEach((path, newValue) -> ConfigHelper.findValue(config, path).ifPresentOrElse(
			value -> trySetValue(id, path, value, newValue),
			() -> logger.warn("Ignoring config value change for unknown value {}/{}", id, path)
		));
	}

	public static ServerboundSetConfigValuesPacket wrap(String modId, Map<ConfigPath, Object> changes) {
		Map<ConfigPath, WrappedConfigValue> wrappedChanges = new HashMap<>();
		changes.forEach((path, value) -> wrappedChanges.put(path, WrappedConfigValue.wrapOrThrow(value)));
		return new ServerboundSetConfigValuesPacket(modId, Collections.unmodifiableMap(wrappedChanges));
	}

	@SuppressWarnings("unchecked")
	private static <T> void trySetValue(ConfigId config, ConfigPath path, ConfigAccess.Value<T> value, WrappedConfigValue newValue) {
		T reference = value.getDefault();

		Optional<String> error = switch (newValue) {
			case Bool(boolean b) when reference instanceof Boolean -> value.set((T) Boolean.valueOf(b));
			case Int(int i) when reference instanceof Integer -> value.set((T) Integer.valueOf(i));
			case Float32(float f) when reference instanceof Float -> value.set((T) Float.valueOf(f));
			case Float64(double d) when reference instanceof Double -> value.set((T) Double.valueOf(d));
			case Str(String s) when reference instanceof String -> value.set((T) s);
			case EnumOrdinal(int ordinal) when reference instanceof Enum<?> e -> {
				Class<?> clazz = e.getDeclaringClass();
				T[] values = (T[]) clazz.getEnumConstants();
				if (ordinal < 0 || ordinal >= values.length) {
					yield Optional.of("Ordinal out of range: %s.values()[%d]".formatted(clazz, ordinal));
				}

				yield value.set(values[ordinal]);
			}

			default -> Optional.of("Type mismatch: " + newValue + " is not " + reference.getClass());
		};

		error.ifPresent(s -> logger.error("Ignoring config value update due to an invalid value [{}/{}]: {}", config, path, s));
	}
}
