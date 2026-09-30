package net.createmod.ponder.api;

import java.util.Random;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.createmod.catnip.api.event.ServerCommandRegistrationCallback;
import net.createmod.catnip.api.lang.LangBuilder;
import net.createmod.ponder.impl.command.PonderCommands;
import net.createmod.ponder.impl.packet.PonderPayloads;
import net.minecraft.resources.Identifier;

public class Ponder {
	public static final String MOD_ID = "ponder";
	public static final String MOD_NAME = "Ponder";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static final Random RANDOM = new Random();

	public static LangBuilder lang() {
		return new LangBuilder(MOD_ID);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Internal
	public static void init() {
		PonderPayloads.register();
		ServerCommandRegistrationCallback.EVENT.subscribe((dispatcher, _, _) -> PonderCommands.register(dispatcher));
	}
}
