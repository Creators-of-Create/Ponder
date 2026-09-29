package net.createmod.catnip.api.config.attribute;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/// A set of [ConfigAttribute]s, with some helpful utilities.
public final class ConfigAttributes implements Iterable<ConfigAttribute> {
	public static final ConfigAttributes NONE = new ConfigAttributes(List.of());

	private final Set<ConfigAttribute> set;

	private ConfigAttributes(Collection<? extends ConfigAttribute> attributes) {
		this.set = Set.copyOf(attributes);
	}

	@Override
	public Iterator<ConfigAttribute> iterator() {
		return this.set.iterator();
	}

	public boolean has(ConfigAttribute.Flag flag) {
		for (ConfigAttribute attribute : this) {
			if (attribute == flag) {
				return true;
			}
		}

		return false;
	}

	public <T extends ConfigAttribute> Optional<T> find(Class<T> type) {
		for (ConfigAttribute attribute : this) {
			if (type.isInstance(attribute)) {
				return Optional.of(type.cast(attribute));
			}
		}

		return Optional.empty();
	}

	public static ConfigAttributes of(ConfigAttribute... attributes) {
		return attributes.length == 0 ? NONE : new ConfigAttributes(Arrays.asList(attributes));
	}
}
