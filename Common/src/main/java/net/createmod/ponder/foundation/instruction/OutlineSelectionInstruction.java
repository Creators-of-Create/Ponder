package net.createmod.ponder.foundation.instruction;

import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.Selection;

public class OutlineSelectionInstruction extends TickingInstruction {

	private PonderPalette color;
	private Object slot;
	private Selection selection;

	public OutlineSelectionInstruction(PonderPalette color, Object slot, Selection selection, int ticks) {
		super(false, ticks);
		this.color = color;
		this.slot = slot;
		this.selection = selection;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		selection.makeOutline(scene.getOutliner(), slot)
			.lineWidth(1 / 16f)
			.colored(color.getColor());
	}

}
