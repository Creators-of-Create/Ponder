package net.createmod.catnip.api.config;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.catnip.api.config.definition.ConfigValue;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

/// Uniquely identifies a [ConfigValue] in a [CatnipConfig].
///
/// Consists of a set of component strings, joined with periods for display, such as `kinetics.disableStress`.
///
/// Always contains at least one component. Valid components only consist of `[a-zA-Z0-9_-]`.
public final class ConfigPath {
	public static final StreamCodec<ByteBuf, ConfigPath> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(64)).map(
		ConfigPath::new, path -> path.components
	);

	public final List<String> components;
	private final String combined;

	/// @throws IllegalArgumentException if any of the given components are invalid
	public ConfigPath(Iterable<String> components) {
		List<String> list = new ArrayList<>();
		StringJoiner joiner = new StringJoiner(".");

		for (String component : components) {
			if (!isValidComponent(component)) {
				throw new IllegalArgumentException("Invalid component: " + component);
			}

			list.add(component);
			joiner.add(component);
		}

		if (list.isEmpty()) {
			throw new IllegalArgumentException("Cannot create an empty ConfigPath");
		}

		this.components = Collections.unmodifiableList(list);
		this.combined = joiner.toString();
	}

	@Override
	public String toString() {
		return this.combined;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ConfigPath that && this.combined.equals(that.combined);
	}

	@Override
	public int hashCode() {
		return this.combined.hashCode();
	}

	public static String assertValidComponent(String component) {
		if (!isValidComponent(component)) {
			throw new IllegalArgumentException("Invalid ConfigPath component: " + component);
		}

		return component;
	}

	/// @return true if the given string is a valid path component
	public static boolean isValidComponent(String component) {
		for (int i = 0; i < component.length(); i++) {
			if (!isAllowedInComponent(component.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/// @return true if the given character is allowed to appear in path components
	public static boolean isAllowedInComponent(char c) {
		// lowercase letters, uppercase letters, and digits
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
			return true;

		// underscores and dashes
		return c == '_' || c == '-';
	}

	public static Builder builder() {
		return new Builder(List.of());
	}

	/// A reusable builder for [ConfigPath]s.
	public static final class Builder {
		private final List<String> components = new ArrayList<>();

		private Builder(Collection<String> initial) {
			this.components.addAll(initial);
		}

		public Builder push(String component) {
			this.components.add(assertValidComponent(component));
			return this;
		}

		public Builder pop() {
			this.components.removeLast();
			return this;
		}

		public ConfigPath build() {
			return new ConfigPath(this.components);
		}

		public ConfigPath buildWith(String component) {
			try {
				this.push(component);
				return this.build();
			} finally {
				this.pop();
			}
		}

		public Builder copyAndPush(String component) {
			Builder copy = new Builder(this.components);
			copy.push(component);
			return copy;
		}

		public void forEachComponent(Consumer<String> consumer) {
			this.components.forEach(consumer);
		}
	}
}
