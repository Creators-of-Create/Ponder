package net.createmod.catnip.impl.config;

import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.api.config.access.ConfigAccess;

import net.createmod.catnip.impl.config.ConfigHelper.FoundElement.Value;
import net.minecraft.commands.Commands;

import net.minecraft.server.permissions.PermissionCheck;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ConfigHelper {;
	// changing configs generally implies file access, restrict it to owners
	public static final PermissionCheck CAN_CHANGE_CONFIGS = Commands.LEVEL_OWNERS;

	public static String toHumanReadable(String key) {
		String s = key.replace("_", " ");
		s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		s = StringUtils.normalizeSpace(s);
		return s;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getActualClass(T object) {
		return (Class<T>) (object instanceof Enum<?> e ? e.getDeclaringClass() : object.getClass());
	}

	public static Optional<String> trySetValue(ConfigValueId id, String valueString) {
		ConfigAccess config = ConfigAccess.FIND.invoker().get(id.config());
		if (config == null) {
			return Optional.of("Couldn't find config " + id.config());
		}

		return findValue(config, id.path())
			.map(value -> trySetValue(value, valueString))
			.orElseGet(() -> Optional.of("Couldn't find config value " + id.path()));
	}

	private static <T> Optional<String> trySetValue(ConfigAccess.Value<T> value, String valueString) {
		Optional<T> deserialized = parseConfigValue(value.getDefault(), valueString);
		if (deserialized.isEmpty()) {
			return Optional.of("Invalid value");
		}

		value.set(deserialized.get());
		return Optional.empty();
	}

	public static Optional<ConfigAccess.Value<?>> findValue(ConfigAccess config, ConfigPath path) {
		return switch (findElement(config, path)) {
			case Value value -> Optional.of(value.value());
			default -> Optional.empty();
		};
	}

	public static FoundElement findElement(ConfigAccess config, ConfigPath path) {
		ConfigAccess currentConfig = config;

		for (int i = 0; i < path.components.size(); i++) {
			String component = path.components.get(i);
			Optional<? extends ConfigAccess.Element> element = currentConfig.getElement(component);
			if (element.isEmpty()) {
				return FoundElement.None.INSTANCE;
			}

			switch (element.get()) {
				case ConfigAccess.Nested nested -> currentConfig = nested;
				case ConfigAccess.Value<?> value -> {
					if (i + 1 == path.components.size()) {
						// last component, found it
						return new Value(currentConfig, value);
					}

					// not the end of the path, but we can't continue through the tree
					return FoundElement.None.INSTANCE;
				}
			}
		}

		// reached the end without hitting a value
		// should be impossible for currentConfig to still be the root
		if (currentConfig == config) {
			throw new IllegalStateException("currentConfig == config");
		}

		return new FoundElement.Nested((ConfigAccess.Nested) currentConfig);
	}

	public static boolean hasAnyConfigs(String modId) {
		for (ConfigSide side : ConfigSide.values()) {
			ConfigId id = new ConfigId(modId, side);
			if (ConfigAccess.FIND.invoker().get(id) != null) {
				return true;
			}
		}

		return false;
	}

	private static <T> Optional<T> parseConfigValue(T reference, String value) {
		try {
			return Optional.of(parseConfigValueUnsafe(reference, value));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	private static <T> T parseConfigValueUnsafe(T reference, String value) throws IllegalArgumentException {
		return (T) switch (reference) {
			case Boolean _ -> switch (value) {
				case "true" -> Boolean.TRUE;
				case "false" -> Boolean.FALSE;
				default -> throw new IllegalArgumentException("Not a boolean: " + value);
			};
			case Integer _ -> Integer.parseInt(value);
			case Float _ -> Float.parseFloat(value);
			case Double _ -> Double.parseDouble(value);
			case String _ -> value;
			case Enum<?> e -> Enum.valueOf(e.getDeclaringClass(), value);
			default -> throw new IllegalArgumentException("Unhandled type: " + reference.getClass());
		};
	}

	public sealed interface FoundElement {
		record Nested(ConfigAccess.Nested nested) implements FoundElement {}
		record Value(ConfigAccess config, ConfigAccess.Value<?> value) implements FoundElement {}
		enum None implements FoundElement { INSTANCE }
	}
}
