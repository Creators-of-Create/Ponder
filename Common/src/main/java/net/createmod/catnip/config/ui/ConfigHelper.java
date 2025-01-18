package net.createmod.catnip.config.ui;

import java.io.Serial;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.packets.ServerboundConfigPacket;
import net.createmod.ponder.Ponder;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHelper {

	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");
	public static final Pattern annotationPattern = Pattern.compile("\\[@cui:([^:]*)(?::(.*))?]");

	public static final Predicate<ModConfig> isForgeConfig = c -> c != null && c.getSpec() instanceof ModConfigSpec;

	public static final Map<String, ConfigChange> changes = new HashMap<>();
	private static final LoadingCache<String, EnumMap<ModConfig.Type, ModConfig>> configCache =
		CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public EnumMap<ModConfig.Type, ModConfig> load(@Nonnull String key) {
					return findModConfigsUncached(key);
				}
			});

	private static EnumMap<ModConfig.Type, ModConfig> findModConfigsUncached(String modID) {
		EnumMap<ModConfig.Type, ModConfig> configMap = new EnumMap<>(ModConfig.Type.class);

		for (ModConfig config : ModConfigs.getModConfigs(modID)) {
			configMap.put(config.getType(), config);
		}

		return configMap;
	}

	public static ModConfigSpec findModConfigSpecFor(ModConfig.Type type, String modID) throws NullPointerException, ClassCastException {
		return (ModConfigSpec) configCache.getUnchecked(modID)
				.get(type)
				.getSpec();
	}

	public static boolean hasAnyForgeConfig(String modID) {
		for (ModConfig config : configCache.getUnchecked(modID).values()) {
			if (isForgeConfig.test(config)) {
				return true;
			}
		}

		return false;
	}

	// Directly set a value
	public static <T> void setConfigValue(ConfigPath path, String value) throws InvalidValueException, ClassCastException, NullPointerException {
		ModConfigSpec spec = findModConfigSpecFor(path.getType(), path.getModID());

		List<String> pathList = Arrays.asList(path.getPath());
		ModConfigSpec.ValueSpec valueSpec = spec.getSpec().getRaw(pathList);
		ModConfigSpec.ConfigValue<T> configValue = spec.getValues().get(pathList);
		T v = (T) ServerboundConfigPacket.deserialize(configValue.get(), value);

		if (!valueSpec.test(v))
			throw new InvalidValueException();

		configValue.set(v);
	}

	// Add a value to the current UI's changes list
	public static <T> void setValue(String path, ModConfigSpec.ConfigValue<T> configValue, T value,
		@Nullable Map<String, String> annotations) {
		if (value.equals(configValue.get())) {
			changes.remove(path);
		} else {
			changes.put(path, annotations == null ? new ConfigChange(value) : new ConfigChange(value, annotations));
		}
	}

	// Get a value from the current UI's changes list or the config value, if its
	// unchanged
	public static <T> T getValue(String path, ModConfigSpec.ConfigValue<T> configValue) {
		ConfigChange configChange = changes.get(path);
		if (configChange != null)
			// noinspection unchecked
			return (T) configChange.value;
		else
			return configValue.get();
	}

	public static Pair<String, Map<String, String>> readMetadataFromComment(List<String> commentLines) {
		AtomicReference<String> unit = new AtomicReference<>();
		Map<String, String> annotations = new HashMap<>();

		commentLines.removeIf(line -> {
			if (line.trim()
				.isEmpty()) {
				return true;
			}

			Matcher matcher = annotationPattern.matcher(line);
			if (matcher.matches()) {
				String annotation = matcher.group(1);
				String aValue = matcher.group(2);
				annotations.putIfAbsent(annotation, aValue);

				return true;
			}

			matcher = unitPattern.matcher(line);
			if (matcher.matches()) {
				unit.set(matcher.group(1));
			}

			return false;
		});

		return Pair.of(unit.get(), annotations);
	}

	public static class ConfigPath {
		private String modID = Ponder.MOD_ID;
		private ModConfig.Type type = ModConfig.Type.CLIENT;
		private String[] path = new String[0];

		public static ConfigPath parse(String string) {
			ConfigPath cp = new ConfigPath();
			String p = string;
			int index = string.indexOf(":");
			if (index >= 0) {
				p = string.substring(index + 1);
				if (index >= 1) {
					cp.modID = string.substring(0, index);
				}
			}
			String[] split = p.split("\\.");
			try {
				cp.type = ModConfig.Type.valueOf(split[0].toUpperCase(Locale.ROOT));
			} catch (Exception e) {
				throw new IllegalArgumentException("path must start with either 'client.', 'common.' or 'server.'");
			}

			cp.path = new String[split.length - 1];
			System.arraycopy(split, 1, cp.path, 0, cp.path.length);

			return cp;
		}

		public String toString() {
			return modID + ":" + type.name().toLowerCase(Locale.ROOT) + "." + String.join(".", path);
		}

		public ConfigPath setID(String modID) {
			this.modID = modID;
			return this;
		}

		public ConfigPath setType(ModConfig.Type type) {
			this.type = type;
			return this;
		}

		public ConfigPath setPath(String[] path) {
			this.path = path;
			return this;
		}

		public String getModID() {
			return modID;
		}

		public ModConfig.Type getType() {
			return type;
		}

		public String[] getPath() {
			return path;
		}
	}

	public static class ConfigChange {
		Object value;
		Map<String, String> annotations = new HashMap<>();

		ConfigChange(Object value) {
			this.value = value;
		}

		ConfigChange(Object value, Map<String, String> annotations) {
			this(value);
			this.annotations.putAll(annotations);
		}
	}

	public static class InvalidValueException extends Exception {
		@Serial private static final long serialVersionUID = 1L;
	}
}
