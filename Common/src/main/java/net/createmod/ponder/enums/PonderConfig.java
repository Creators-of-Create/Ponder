package net.createmod.ponder.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.neoforged.fml.config.ModConfig;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.config.CClient;

public class PonderConfig {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	@Nullable private static CClient client;

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static Set<Map.Entry<ModConfig.Type, ConfigBase>> registerConfigs() {
		client = register(CClient::new, ModConfig.Type.CLIENT);
		//COMMON = register(CCommon::new, ModConfig.Type.COMMON);
		//SERVER = register(CServer::new, ModConfig.Type.SERVER);

		return CONFIGS.entrySet();
	}

	public static void onLoad(ModConfig config) {
		for (ConfigBase configBase : CONFIGS.values())
			if (configBase.specification == config.getSpec())
				configBase.onLoad();
	}

	public static void onReload(ModConfig config) {
		for (ConfigBase configBase : CONFIGS.values())
			if (configBase.specification == config.getSpec())
				configBase.onReload();
	}

	public static CClient Client() {
		if (client == null)
			throw new AssertionError("Ponder Client Config was accessed, but not registered yet!");

		return client;
	}

}
