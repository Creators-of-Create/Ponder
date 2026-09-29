package net.createmod.catnip.impl.config.packet;

import com.mojang.datafixers.util.Either;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class OpenConfigScreenPacket implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, OpenConfigScreenPacket> CODEC = ByteBufCodecs.either(
		ByteBufCodecs.STRING_UTF8, ByteBufCodecs.either(ConfigId.STREAM_CODEC, ConfigValueId.STREAM_CODEC)
	).map(OpenConfigScreenPacket::new, packet -> packet.modOrConfigOrValueId);

	private final Either<String, Either<ConfigId, ConfigValueId>> modOrConfigOrValueId;

	private OpenConfigScreenPacket(Either<String, Either<ConfigId, ConfigValueId>> either) {
		this.modOrConfigOrValueId = either;
	}

	public Target target() {
		return this.modOrConfigOrValueId.map(Target.Mod::new, either -> either.map(Target.Config::new, Target.Value::new));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return CatnipPayloads.OPEN_CONFIG_SCREEN;
	}

	public static OpenConfigScreenPacket forMod(String id) {
		return new OpenConfigScreenPacket(Either.left(id));
	}

	public static OpenConfigScreenPacket forConfig(ConfigId id) {
		return new OpenConfigScreenPacket(Either.right(Either.left(id)));
	}

	public static OpenConfigScreenPacket forValue(ConfigValueId id) {
		return new OpenConfigScreenPacket(Either.right(Either.right(id)));
	}

	public sealed interface Target {
		record Mod(String id) implements Target {}
		record Config(ConfigId id) implements Target {}
		record Value(ConfigValueId id) implements Target {}
	}
}
