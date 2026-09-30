package net.createmod.ponder.impl.packet;

import net.createmod.catnip.api.network.registry.CatnipPayloadRegistrar;
import net.createmod.ponder.api.Ponder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public final class PonderPayloads {
	private static final CatnipPayloadRegistrar registrar = new CatnipPayloadRegistrar(Ponder.MOD_ID);

	public static final Type<ReloadPonderPacket> RELOAD_PONDER = registrar.clientbound("reload_ponder", ReloadPonderPacket.CODEC);
	public static final Type<OpenPonderPacket> OPEN_PONDER = registrar.clientbound("open_ponder", OpenPonderPacket.CODEC);

	public static void register() {}
}
