package net.createmod.ponder.foundation.instruction;

import java.util.function.UnaryOperator;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlocksInstruction extends WorldModifyInstruction {

	private UnaryOperator<BlockState> stateToUse;
	private boolean replaceAir;
	private boolean spawnParticles;

	public ReplaceBlocksInstruction(Selection selection, UnaryOperator<BlockState> stateToUse, boolean replaceAir,
		boolean spawnParticles) {
		super(selection);
		this.stateToUse = stateToUse;
		this.replaceAir = replaceAir;
		this.spawnParticles = spawnParticles;
	}

	@Override
	protected void runModification(Selection selection, PonderScene scene) {
		PonderLevel level = scene.getWorld();
		selection.forEach(pos -> {
			if (!level.getBounds()
				.isInside(pos))
				return;
			BlockState prevState = level.getBlockState(pos);
			if (!replaceAir && prevState == Blocks.AIR.defaultBlockState())
				return;
			if (spawnParticles)
				level.addBlockDestroyEffects(pos, prevState);
			level.setBlockAndUpdate(pos, stateToUse.apply(prevState));
		});
	}

	@Override
	protected boolean needsRedraw() {
		return true;
	}

}
