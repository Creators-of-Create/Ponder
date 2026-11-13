package net.createmod.catnip.api;

import net.createmod.catnip.api.commands.CommandArgumentTypeRegistry;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.api.event.ServerCommandRegistrationCallback;
import net.createmod.catnip.impl.command.CatnipCommands;
import net.createmod.catnip.impl.command.argument.ConfigIdArgument;
import net.createmod.catnip.impl.command.argument.ConfigPathArgument;
import net.createmod.catnip.impl.command.argument.ModIdArgument;
import net.createmod.catnip.impl.config.CatnipConfigAccess;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.minecraft.resources.Identifier;

public final class Catnip {
	public static final String ID = "catnip";

	// Called from the constructor on NeoForge, and from onInitialize on Fabric
	public static void earlyInit() {
		CommandArgumentTypeRegistry.INSTANCE.register(id("config_id"), ConfigIdArgument.class, ConfigIdArgument.INFO);
		CommandArgumentTypeRegistry.INSTANCE.register(id("config_path"), ConfigPathArgument.class, ConfigPathArgument.INFO);
		CommandArgumentTypeRegistry.INSTANCE.register(id("mod_id"), ModIdArgument.class, ModIdArgument.INFO);
	}

	public static void init() {
		CatnipPayloads.init();

		ServerCommandRegistrationCallback.EVENT.subscribe((dispatcher, _, _) -> CatnipCommands.register(dispatcher));

		ConfigAccess.FIND.subscribe(CatnipConfigAccess::find);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
