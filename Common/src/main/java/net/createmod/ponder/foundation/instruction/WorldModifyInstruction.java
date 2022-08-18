package net.createmod.ponder.foundation.instruction;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.WorldSectionElement;

public abstract class WorldModifyInstruction extends PonderInstruction {

	private Selection selection;

	public WorldModifyInstruction(Selection selection) {
		this.selection = selection;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(PonderScene scene) {
		runModification(selection, scene);
		if (needsRedraw()) 
			scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
	}

	protected abstract void runModification(Selection selection, PonderScene scene);

	protected abstract boolean needsRedraw();

}
