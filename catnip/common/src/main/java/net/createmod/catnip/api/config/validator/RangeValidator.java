package net.createmod.catnip.api.config.validator;

import java.util.Optional;

public record RangeValidator<T extends Number>(T min, T max) implements Validator<T> {
	@Override
	public Optional<String> test(T value) {
		if (value.doubleValue() < this.min.doubleValue()) {
			return Optional.of(value + " is less than the minimum of " + this.min);
		} else if (value.doubleValue() > this.max.doubleValue()) {
			return Optional.of(value + " is greater than the maximum of " + this.max);
		} else {
			return Optional.empty();
		}
	}
}
