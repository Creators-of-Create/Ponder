package net.createmod.ponder.impl.client.instruction;

import java.util.function.UnaryOperator;

import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.api.client.scene.Selection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlocksInstruction extends WorldModifyInstruction {
	private final UnaryOperator<BlockState> stateToUse;
	private final boolean replaceAir;
	private final boolean spawnParticles;

	public ReplaceBlocksInstruction(Selection selection, UnaryOperator<BlockState> stateToUse, boolean replaceAir,
									boolean spawnParticles) {
		super(selection);
		this.stateToUse = stateToUse;
		this.replaceAir = replaceAir;
		this.spawnParticles = spawnParticles;
	}

	@Override
	protected void runModification(Selection selection, PonderScene scene) {
		PonderLevel level = scene.getLevel();
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
