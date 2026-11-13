package net.createmod.ponder.impl.client.instruction;

import java.util.function.UnaryOperator;

import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.api.client.scene.Selection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class BlockEntityDataInstruction extends WorldModifyInstruction {
	private final boolean redraw;
	private final UnaryOperator<CompoundTag> data;
	private final Class<? extends BlockEntity> type;

	public BlockEntityDataInstruction(Selection selection, Class<? extends BlockEntity> type,
									  UnaryOperator<CompoundTag> data, boolean redraw) {
		super(selection);
		this.type = type;
		this.data = data;
		this.redraw = redraw;
	}

	@Override
	protected void runModification(Selection selection, PonderScene scene) {
		PonderLevel level = scene.getLevel();
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
			try (ProblemReporter.ScopedCollector problems = new ProblemReporter.ScopedCollector(this::toString, Ponder.LOGGER)) {
				ValueInput in = TagValueInput.create(problems, level.registryAccess(), apply);
				blockEntity.loadWithComponents(in);
			}
		});
	}

	@Override
	protected boolean needsRedraw() {
		return redraw;
	}
}
