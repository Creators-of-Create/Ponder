package net.createmod.catnip.impl.network;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.network.registry.CatnipPayloadRegistrar;
import net.createmod.catnip.impl.config.packet.ClientboundSetConfigValuePacket;
import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket;
import net.createmod.catnip.impl.config.packet.ServerboundSetConfigValuesPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

/// @see net.createmod.catnip.impl.client.CatnipClientPayloadHandlers
@SuppressWarnings("JavadocReference")
public final class CatnipPayloads {
	private static final CatnipPayloadRegistrar registrar = new CatnipPayloadRegistrar(Catnip.ID);

	// clientbound
	public static final Type<OpenConfigScreenPacket> OPEN_CONFIG_SCREEN = registrar.clientbound("open_config_screen", OpenConfigScreenPacket.CODEC);
	public static final Type<ClientboundSetConfigValuePacket> SET_CONFIG_VALUE = registrar.clientbound("set_config_value", ClientboundSetConfigValuePacket.CODEC);

	// serverbound
	public static final Type<ServerboundSetConfigValuesPacket> SET_CONFIG_VALUES = registrar.selfHandlingServerbound("set_config_values", ServerboundSetConfigValuesPacket.STREAM_CODEC);

	public static void init() {}
}
