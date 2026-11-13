package net.createmod.ponder.api.client.scene;

import net.createmod.ponder.api.client.level.PonderLevel;

@FunctionalInterface
public interface ParticleEmitter {
	void create(PonderLevel level, double x, double y, double z);
}
