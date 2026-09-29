package net.createmod.catnip.api.config.definition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.StringJoiner;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.ConfigPath.Builder;
import net.createmod.catnip.api.config.validator.RangeValidator;
import net.createmod.catnip.api.config.validator.Validator;

import net.createmod.catnip.impl.config.CatnipModConfig;

import org.jspecify.annotations.Nullable;

/// Base class for a config definition built with Catnip.
///
/// To create a config of your own, extend [Root] and define values with the value builder methods.
/// For an example, see [CatnipModConfig].
/// @see CatnipConfigRegistry
public abstract sealed class CatnipConfig {
	private final SequencedMap<String, ConfigValue<?>> values = new LinkedHashMap<>();
	private final SequencedMap<String, CatnipConfig.Nested> nested = new LinkedHashMap<>();
	private final ConfigPath.Builder pathBuilder;

	protected CatnipConfig(Builder pathBuilder) {
		this.pathBuilder = pathBuilder;
	}

	/// The depth of this config in its tree. 0 for [root][Root] configs.
	public abstract int depth();

	/// @return an immutable view of this config's values, mapped by name
	public final SequencedMap<String, ConfigValue<?>> valueMap() {
		return Collections.unmodifiableSequencedMap(this.values);
	}

	/// @return an immutable view of this config's values
	public final SequencedCollection<ConfigValue<?>> values() {
		return this.valueMap().sequencedValues();
	}

	/// @return an immutable view of this config's nested configs
	public final SequencedMap<String, CatnipConfig.Nested> nested() {
		return Collections.unmodifiableSequencedMap(this.nested);
	}

	/// Invoked after this config has been loaded for the first time.
	/// This could either be from a file, or from the default values.
	public void onLoad() {
		this.nested.values().forEach(CatnipConfig::onLoad);
	}

	/// Invoked after each subsequent load of this config.
	public void onReload() {
		this.nested.values().forEach(CatnipConfig::onReload);
	}

	// ----- value builders -----

	protected final ConfigValue<Boolean> b(boolean defaultValue, String name, String... comment) {
		return this.value(defaultValue, name, comment);
	}

	protected final ConfigValue<Float> f(float defaultValue, float min, float max, String name, String... comment) {
		Validator<Float> validator = new RangeValidator<>(min, max);
		return this.value(defaultValue, name, validator, comment);
	}

	protected final ConfigValue<Float> f(float defaultValue, float min, String name, String... comment) {
		return this.f(defaultValue, min, Float.MAX_VALUE, name, comment);
	}

	protected final ConfigValue<Integer> i(int defaultValue, int min, int max, String name, String... comment) {
		Validator<Integer> validator = new RangeValidator<>(min, max);
		return this.value(defaultValue, name, validator, comment);
	}

	protected final ConfigValue<Integer> i(int defaultValue, int min, String name, String... comment) {
		return this.i(defaultValue, min, Integer.MAX_VALUE, name, comment);
	}

	protected final ConfigValue<Integer> i(int defaultValue, String name, String... comment) {
		return this.i(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, name, comment);
	}

	protected final <T extends Enum<T>> ConfigValue<T> e(T defaultValue, String name, String... comment) {
		return this.value(defaultValue, name, comment);
	}

	protected final <T> ConfigValue<T> value(T defaultValue, String name, String... comment) {
		return this.value(defaultValue, name, null, comment);
	}

	protected final <T> ConfigValue<T> value(T defaultValue, String name, @Nullable Validator<T> validator, String... comment) {
		if (this.values.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate config value named " + name);
		} else if (this.nested.containsKey(name)) {
			throw new IllegalArgumentException("A config value cannot have the same name as a nested config: " + name);
		}

		ConfigPath path = this.pathBuilder.buildWith(name);
		String mergedComments = mergeComments(comment);
		ConfigValue<T> value = new ConfigValue<>(path, mergedComments, defaultValue, validator);
		this.values.put(name, value);
		return value;
	}

	protected final <T extends CatnipConfig.Nested> T nested(CatnipConfig.Nested.Factory<T> factory, String name, String comment) {
		if (!ConfigPath.isValidComponent(name)) {
			throw new IllegalArgumentException("Invalid nested config name: " + name);
		} else if (this.values.containsKey(name)) {
			throw new IllegalArgumentException("A nested config cannot have the same name as a value: " + name);
		}

		T nested = factory.create(this, name, comment);

		if (!name.equals(nested.name())) {
			throw new IllegalStateException("Name does not match: " + name + " / " + nested.name());
		} else if (this.nested.putIfAbsent(name, nested) != null) {
			throw new IllegalArgumentException("Duplicate nested config named " + name);
		}

		return nested;
	}

	// ----- helpers -----

	@Nullable
	private static String mergeComments(String... comments) {
		if (comments.length == 0) {
			return null;
		} else if (comments.length == 1) {
			return comments[0];
		}

		StringJoiner joiner = new StringJoiner("\n");

		for (String comment : comments) {
			joiner.add(comment);
		}

		return joiner.toString();
	}

	public static abstract non-sealed class Root extends CatnipConfig {
		protected Root() {
			super(ConfigPath.builder());
		}

		@Override
		public final int depth() {
			return 0;
		}
	}

	public static abstract non-sealed class Nested extends CatnipConfig implements ConfigElement {
		public final CatnipConfig parent;

		private final String name;
		private final String comment;

		protected Nested(CatnipConfig parent, String name, String comment) {
			super(parent.pathBuilder.copyAndPush(name));
			this.parent = parent;
			this.name = name;
			this.comment = comment;
		}

		@Override
		public final int depth() {
			return this.parent.depth() + 1;
		}

		@Override
		public final String name() {
			return this.name;
		}

		@Override
		public final String comment() {
			return this.comment;
		}

		@FunctionalInterface
		public interface Factory<T extends CatnipConfig.Nested> {
			T create(CatnipConfig parent, String name, String comment);
		}
	}
}
