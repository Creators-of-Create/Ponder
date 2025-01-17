package net.createmod.ponder;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceLocation;

public class Ponder {

	public static final String MOD_ID = "ponder";
	public static final String MOD_NAME = "Ponder";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static final Random RANDOM = new Random();

	public static LangBuilder lang() {
		return new LangBuilder(MOD_ID);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static void init() {

	}

}
