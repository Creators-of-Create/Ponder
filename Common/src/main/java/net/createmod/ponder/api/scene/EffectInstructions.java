package net.createmod.ponder.api.scene;

import net.createmod.ponder.foundation.instruction.EmitParticlesInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface EffectInstructions {
	void emitParticles(Vec3 location, EmitParticlesInstruction.Emitter emitter, float amountPerCycle, int cycles);

	void indicateRedstone(BlockPos pos);

	void indicateSuccess(BlockPos pos);

	void createRedstoneParticles(BlockPos pos, int color, int amount);
}
