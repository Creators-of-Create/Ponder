package net.createmod.ponder.foundation.instruction;

import java.util.function.UnaryOperator;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityDataInstruction extends WorldModifyInstruction {

	private boolean redraw;
	private UnaryOperator<CompoundTag> data;
	private Class<? extends BlockEntity> type;

	public BlockEntityDataInstruction(Selection selection, Class<? extends BlockEntity> type,
									  UnaryOperator<CompoundTag> data, boolean redraw) {
		super(selection);
		this.type = type;
		this.data = data;
		this.redraw = redraw;
	}

	@Override
	protected void runModification(Selection selection, PonderScene scene) {
		PonderLevel level = scene.getWorld();
		selection.forEach(pos -> {
			if (!level.getBounds()
					.isInside(pos))
				return;
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!type.isInstance(blockEntity))
				return;
			CompoundTag apply = data.apply(blockEntity.saveWithFullMetadata(level.registryAccess()));
			//if (blockEntity instanceof SyncedBlockEntity) //TODO
			//	((SyncedBlockEntity) blockEntity).readClient(apply);
			blockEntity.loadWithComponents(apply, level.registryAccess());
		});
	}

	@Override
	protected boolean needsRedraw() {
		return redraw;
	}

}
