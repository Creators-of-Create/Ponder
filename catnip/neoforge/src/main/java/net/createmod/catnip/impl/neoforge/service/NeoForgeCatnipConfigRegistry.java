package net.createmod.catnip.impl.neoforge.service;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.definition.CatnipConfig;

import net.createmod.catnip.api.config.definition.ConfigValue;

import net.createmod.catnip.api.config.validator.RangeValidator;
import net.createmod.catnip.api.config.validator.Validator;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class NeoForgeCatnipConfigRegistry implements CatnipConfigRegistry {
	private static final Map<ConfigId, CatnipConfig.Root> registeredConfigs = new HashMap<>();

	@Override
	public void register(ConfigId id, CatnipConfig.Root config) {
		if (registeredConfigs.putIfAbsent(id, config) != null) {
			throw new IllegalArgumentException("Duplicate config registration with ID " + id);
		}

		ModContainer container = ModList.get().getModContainerById(id.modId()).orElseThrow(
			() -> new IllegalArgumentException("No loaded mod with ID " + id.modId())
		);

		IEventBus bus = container.getEventBus();
		if (bus == null) {
			throw new IllegalStateException("Mod " + id.modId() + " has no event bus, cannot register configs!");
		}

		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		List<Runnable> valueSyncers = new ArrayList<>();
		walk(builder, config, valueSyncers);
		ModConfigSpec spec = builder.build();

		ModConfig.Type type = switch (id.side()) {
			case CLIENT -> Type.CLIENT;
			case COMMON -> Type.COMMON;
			case SERVER -> Type.SERVER;
		};

		container.registerConfig(type, spec);

		bus.addListener(ModConfigEvent.Loading.class, event -> {
			if (event.getConfig().getSpec() == spec) {
				valueSyncers.forEach(Runnable::run);
				config.onLoad();
			}
		});

		bus.addListener(ModConfigEvent.Reloading.class, event -> {
			if (event.getConfig().getSpec() == spec) {
				valueSyncers.forEach(Runnable::run);
				config.onReload();
			}
		});
	}

	@Override
	public Optional<CatnipConfig.Root> get(ConfigId id) {
		return Optional.ofNullable(registeredConfigs.get(id));
	}

	private static void walk(ModConfigSpec.Builder builder, CatnipConfig config, List<Runnable> valueSyncers) {
		config.values().forEach(value -> valueSyncers.add(define(builder, value)));
		config.nested().sequencedValues().forEach(nested -> walk(builder, nested, valueSyncers));
	}

	/// @return a runnable that syncs the value from forge to catnip when invoked
	private static <T> Runnable define(ModConfigSpec.Builder builder, ConfigValue<T> value) {
		Class<T> clazz = ConfigHelper.getActualClass(value.defaultValue);
		Predicate<Object> validator = wrapValidator(clazz, value.validator);

		builder.comment(value.comment());
		builder.comment(" Default: " + value.defaultValue);

		if (value.validator instanceof RangeValidator<?>(Number min, Number max)) {
			builder.comment(" Min: " + min);
			builder.comment(" Max: " + max);
		}

		T[] enumConstants = clazz.getEnumConstants();
		if (enumConstants != null) {
			builder.comment(" Allowed values: " + Arrays.toString(enumConstants));
		}

		ModConfigSpec.ConfigValue<T> defined = builder.define(value.path.components, () -> value.defaultValue, validator, clazz);

		// sync changes back and forth
		value.registerChangeCallback((_, newValue) -> {
			defined.set(newValue);
			// this is pretty inefficient if multiple values change at once, but it should be Fine™
			defined.save();
		});

		return () -> value.set(defined.get());
	}

	private static <T> Predicate<Object> wrapValidator(Class<T> clazz, @Nullable Validator<T> validator) {
		return validator == null ? (_ -> true) : object -> {
			if (clazz.isInstance(object)) {
				T casted = clazz.cast(object);
				return validator.test(casted).isEmpty();
			}

			return false;
		};
	}
}
