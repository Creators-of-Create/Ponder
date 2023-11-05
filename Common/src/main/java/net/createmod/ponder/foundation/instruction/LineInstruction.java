package net.createmod.ponder.foundation.instruction;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.world.phys.Vec3;

public class LineInstruction extends TickingInstruction {

	private PonderPalette color;
	private Vec3 start;
	private Vec3 end;
	private boolean big;

	public LineInstruction(PonderPalette color, Vec3 start, Vec3 end, int ticks, boolean big) {
		super(false, ticks);
		this.color = color;
		this.start = start;
		this.end = end;
		this.big = big;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		scene.getOutliner()
			.showLine(start, start, end)
			.lineWidth(big ? 1 / 8f : 1 / 16f)
			.colored(color.getColor());
	}

}
