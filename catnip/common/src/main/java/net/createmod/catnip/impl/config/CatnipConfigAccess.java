package net.createmod.catnip.impl.config;

import com.google.common.collect.Iterators;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.access.ConfigAccess;

import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.catnip.api.config.definition.ConfigValue;

import net.createmod.catnip.api.config.validator.RangeValidator;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

public sealed class CatnipConfigAccess implements ConfigAccess {
	protected final CatnipConfig config;

	private CatnipConfigAccess(CatnipConfig config) {
		this.config = config;
	}

	@Override
	public Iterator<? extends Element> elements() {
		return Iterators.concat(
			this.config.nested().values().stream().map(Nested::new).iterator(),
			this.config.values().stream().map(Value::new).iterator()
		);
	}

	@Override
	public Optional<? extends ConfigAccess.Element> getElement(String name) {
		ConfigValue<?> value = this.config.valueMap().get(name);
		if (value != null) {
			return Optional.of(new Value<>(value));
		}

		CatnipConfig.Nested nested = this.config.nested().get(name);
		return nested == null ? Optional.empty() : Optional.of(new Nested(nested));
	}

	public static @Nullable ConfigAccess find(ConfigId id) {
		return CatnipConfigRegistry.INSTANCE.get(id).map(CatnipConfigAccess::new).orElse(null);
	}

	private static final class Nested extends CatnipConfigAccess implements ConfigAccess.Nested {
		private Nested(CatnipConfig.Nested config) {
			super(config);
		}

		@Override
		public String name() {
			return ((CatnipConfig.Nested) this.config).name();
		}

		@Override
		public String comment() {
			return ((CatnipConfig.Nested) this.config).comment();
		}
	}

	private record Value<T>(ConfigValue<T> value) implements ConfigAccess.Value<T> {
		@Override
		public T get() {
			return this.value.get();
		}

		@Override
		public T getDefault() {
			return this.value.defaultValue;
		}

		@Override
		public Optional<String> set(T value) {
			return this.value.trySet(value);
		}

		@Override
		public Optional<String> validate(T value) {
			return this.value.validate(value);
		}

		@Override
		public ConfigAttributes attributes() {
			if (this.value.validator instanceof RangeValidator<?>(Number min, Number max)) {
				return ConfigAttributes.of(
					new ConfigAttribute.Minimum<>(min),
					new ConfigAttribute.Maximum<>(max)
				);
			}

			return ConfigAttributes.NONE;
		}

		@Override
		public String name() {
			return this.value.name();
		}

		@Override
		public String comment() {
			return this.value.comment();
		}
	}
}
