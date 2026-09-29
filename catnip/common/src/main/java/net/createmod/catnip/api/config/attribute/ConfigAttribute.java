package net.createmod.catnip.api.config.attribute;

/// A config value may have a set of attributes, which Catnip can use to augment its behavior in the GUI.
public sealed interface ConfigAttribute {
	enum Flag implements ConfigAttribute {
		/// Indicates that the value requires a relog to apply. Will be displayed in the tooltip.
		REQUIRES_RELOG,
		/// Indicates that the value requires a restart to apply. Will be displayed in the tooltip.
		REQUIRES_RESTART
	}

	/// Indicates that the value has a minimum. Will be displayed next to the option and in the tooltip.
	record Minimum<T extends Number>(T value) implements ConfigAttribute {}
	/// Indicates that the value has a maximum. Will be displayed next to the option and in the tooltip.
	record Maximum<T extends Number>(T value) implements ConfigAttribute {}

	/// Indicates that the value uses a certain kind of units. Will be displayed as-is. Examples:
	/// - `In blocks`
	/// - `In Hex: [#RRGGBB]`
	/// - `In SU`
	record Units(String value) implements ConfigAttribute {}

	enum IntDisplay implements ConfigAttribute {
		HEX("#"),
		ZERO_X("0x"),
		ZERO_B("0b");

		public final String prefix;

		IntDisplay(String prefix) {
			this.prefix = prefix;
		}
	}
}
