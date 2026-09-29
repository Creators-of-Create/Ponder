package net.createmod.catnip.api.config.definition;

import net.createmod.catnip.api.config.ConfigPath;

/// An element of a [CatnipConfig]. Has a name and possibly a comment.
public sealed interface ConfigElement permits ConfigValue, CatnipConfig.Nested {
	/// The name of this element. Must be a valid component of a [ConfigPath].
	String name();
	/// A comment associated with this element. May be empty.
	String comment();
}
