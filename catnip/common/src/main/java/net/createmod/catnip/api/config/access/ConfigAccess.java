package net.createmod.catnip.api.config.access;

import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.attribute.ConfigAttribute;
import net.createmod.catnip.api.config.attribute.ConfigAttributes;
import net.createmod.catnip.api.config.definition.CatnipConfig;

import net.createmod.catnip.api.event.CatnipEvent;

import net.createmod.catnip.impl.config.CatnipConfigAccess;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

/// Provides a mutable view into an arbitrary config, so Catnip can edit it via GUI or commands.
///
/// Each config has a set of uniquely-named [elements][Element]. An element is either a [Value][Value] or a [nested config][Nested].
/// This forms a tree with [ConfigAccess] branches and [Value][Value] leaves.
///
/// Instances of this class and its [elements][Element] are ephemeral.
/// Catnip will [request one][#FIND] when needed, read/write its values, and then discard it.
///
/// This is distinct from [CatnipConfig]: CatnipConfig is one of many possible implementations of this API.
///
/// For an example implementation, see [CatnipConfigAccess].
public interface ConfigAccess {
	/// Event invoked by Catnip when searching for a [ConfigAccess].
	/// Providers will be invoked in registration order. The first returned non-null value will be used.
	CatnipEvent<Provider> FIND = CatnipEvent.create(providers -> id -> {
		for (Provider provider : providers) {
			ConfigAccess access = provider.get(id);
			if (access != null) {
				return access;
			}
		}

		return null;
	});

	/// @return an iterator over all of this config's elements
	Iterator<? extends Element> elements();

	/// @return the element with the given name, if present
	Optional<? extends Element> getElement(String name);

	/// A nested config.
	non-sealed interface Nested extends ConfigAccess, Element {}

	/// An element of a config. Either a [Value][Value] or a [nested config][Nested].
	sealed interface Element {
		/// The name of this element. Must be a valid [ConfigPath] component.
		String name();
		/// A comment that goes with this element. May be empty.
		String comment();
	}

	/// A configurable value.
	non-sealed interface Value<T> extends Element {
		T get();
		T getDefault();

		/// Try to set this value.
		/// @return an error message if the value is [invalid][#validate(Object)]
		Optional<String> set(T value);

		/// Validate the given value, to the greatest extent possible.
		/// @return an error message if the value is invalid (ex. out of range)
		Optional<String> validate(T value);

		/// @return this value's set of [ConfigAttribute]s
		ConfigAttributes attributes();
	}

	@FunctionalInterface
	interface Provider {
		/// Try to find a [ConfigAccess] for the given [ConfigId].
		/// @return a found [ConfigAccess], or null if one was not found
		@Nullable ConfigAccess get(ConfigId id);
	}
}
