package net.createmod.catnip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.createmod.catnip.utility.lang.LangBuilder;
import net.minecraft.resources.ResourceLocation;

public class Catnip {

	public static final String MOD_ID = "catnip";
	public static final String MOD_NAME = "Catnip";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static LangBuilder lang() {
		return new LangBuilder(MOD_ID);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static void init() {

	}

}