package net.createmod.catnip.utility.flw;

public interface Scale<Self> {
	Self scale(float factorX, float factorY, float factorZ);

	default Self scale(float factor) {
		return scale(factor, factor, factor);
	}
}