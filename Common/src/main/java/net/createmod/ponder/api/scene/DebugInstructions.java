package net.createmod.ponder.api.scene;

import java.util.function.Consumer;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;

public interface DebugInstructions {
	void debugSchematic();

	void addInstructionInstance(PonderInstruction instruction);

	void enqueueCallback(Consumer<PonderScene> callback);
}
