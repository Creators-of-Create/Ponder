package net.createmod.ponder.impl.client.instruction;

import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.scene.ParticleEmitter;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.minecraft.world.phys.Vec3;

public class EmitParticlesInstruction extends TickingInstruction {
	private final Vec3 anchor;
	private final ParticleEmitter emitter;
	private final float runsPerTick;

	public EmitParticlesInstruction(Vec3 anchor, ParticleEmitter emitter, float runsPerTick, int ticks) {
		super(false, ticks);
		this.anchor = anchor;
		this.emitter = emitter;
		this.runsPerTick = runsPerTick;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int runs = (int) runsPerTick;
		if (Ponder.RANDOM.nextFloat() < (runsPerTick - runs))
			runs++;
		for (int i = 0; i < runs; i++)
			emitter.create(scene.getLevel(), anchor.x, anchor.y, anchor.z);
	}
}
