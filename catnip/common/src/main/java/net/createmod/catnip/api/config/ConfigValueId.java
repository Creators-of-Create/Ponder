package net.createmod.catnip.api.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/// Uniquely identifies a config value by its owner config and its path within it.
public record ConfigValueId(ConfigId config, ConfigPath path) {
	public static final StreamCodec<ByteBuf, ConfigValueId> STREAM_CODEC = StreamCodec.composite(
		ConfigId.STREAM_CODEC, ConfigValueId::config,
		ConfigPath.STREAM_CODEC, ConfigValueId::path,
		ConfigValueId::new
	);

	@Override
	public String toString() {
		return this.config.toString() + '/' + this.path;
	}
}
