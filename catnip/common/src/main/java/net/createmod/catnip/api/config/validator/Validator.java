package net.createmod.catnip.api.config.validator;

import java.util.Optional;

/// A validator for a config value.
public interface Validator<T> {
	/// Determine if the given value is allowed.
	/// @return an error message if the value is not valid, otherwise empty
	Optional<String> test(T value);
}
