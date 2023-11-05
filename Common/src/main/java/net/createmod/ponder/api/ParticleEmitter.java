package net.createmod.ponder.api;

import net.createmod.ponder.api.level.PonderLevel;

@FunctionalInterface
public interface ParticleEmitter {
	void create(PonderLevel world, double x, double y, double z);
}
