package net.createmod.catnip.api.config;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Locale;
import java.util.function.IntFunction;

public enum ConfigSide {
	CLIENT,
	COMMON,
	SERVER;

	public static final StreamCodec<ByteBuf, ConfigSide> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ConfigSide.class);

	@Override
	public String toString() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
