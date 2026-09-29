package net.createmod.catnip.api.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.regex.Pattern;

/// Identifies a config belonging to a mod.
public record ConfigId(String modId, ConfigSide side) {
	public static final StreamCodec<ByteBuf, ConfigId> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ConfigId::modId,
		ConfigSide.STREAM_CODEC, ConfigId::side,
		ConfigId::new
	);

	// lowest common denominator across fabric and forge.
	// fabric allows: lowercase letters, dashes, underscores
	// forge allows: 2-64 chars, lowercase letters, digits, underscores, periods (when followed by a letter)
	// together:
	// - no length limit
	// - lowercase letters, digits, dashes and underscores
	// - periods when followed by a letter
	// based on the forge regex. just removed the length limit and added dashes
	private static final Pattern modIdPattern = Pattern.compile("^(?=.*$)[a-z][a-z0-9_-]*(\\.[a-z][a-z0-9_]*)*$");

	public ConfigId {
		if (!modIdPattern.asMatchPredicate().test(modId)) {
			throw new IllegalArgumentException("Invalid mod ID: " + modId);
		}
	}

	@Override
	public String toString() {
		return this.modId + ':' + this.side;
	}
}
