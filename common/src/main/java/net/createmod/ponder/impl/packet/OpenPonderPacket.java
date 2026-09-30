package net.createmod.ponder.impl.packet;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenPonderPacket(Target target) implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, OpenPonderPacket> CODEC = Target.STREAM_CODEC.map(OpenPonderPacket::new, OpenPonderPacket::target);

	public static final OpenPonderPacket INDEX = new OpenPonderPacket(Target.Index.INSTANCE);
	public static final OpenPonderPacket TAGS = new OpenPonderPacket(Target.Tags.INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PonderPayloads.OPEN_PONDER;
	}

	public static OpenPonderPacket scenes(Identifier id) {
		return new OpenPonderPacket(new Target.Scenes(id));
	}

	public sealed interface Target {
		StreamCodec<ByteBuf, Target> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Type::of, type -> type.streamCodec);

		enum Index implements Target { INSTANCE }
		enum Tags implements Target { INSTANCE }
		record Scenes(Identifier id) implements Target {}

		enum Type {
			INDEX(StreamCodec.unit(Index.INSTANCE)),
			TAGS(StreamCodec.unit(Tags.INSTANCE)),
			SCENE(Identifier.STREAM_CODEC.map(Scenes::new, Scenes::id));

			public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Type.class);

			public final StreamCodec<ByteBuf, ? extends Target> streamCodec;

			Type(StreamCodec<ByteBuf, ? extends Target> streamCodec) {
				this.streamCodec = streamCodec;
			}

			public static Type of(Target target) {
				return switch (target) {
					case Index _ -> INDEX;
					case Tags _ -> TAGS;
					case Scenes _ -> SCENE;
				};
			}
		}
	}
}
