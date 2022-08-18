package net.createmod.ponder.foundation.instruction;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class DisplayWorldSectionInstruction extends FadeIntoSceneInstruction<WorldSectionElement> {

	private final Selection initialSelection;
	@Nullable private final Supplier<WorldSectionElement> mergeOnto;
	private BlockPos glue;

	public DisplayWorldSectionInstruction(int fadeInTicks, Direction fadeInFrom, Selection selection,
		@Nullable Supplier<WorldSectionElement> mergeOnto) {
		this(fadeInTicks, fadeInFrom, selection, mergeOnto, null);
	}

	public DisplayWorldSectionInstruction(int fadeInTicks, Direction fadeInFrom, Selection selection,
		@Nullable Supplier<WorldSectionElement> mergeOnto, @Nullable BlockPos glue) {
		super(fadeInTicks, fadeInFrom, new WorldSectionElement(selection));
		initialSelection = selection;
		this.mergeOnto = mergeOnto;
		this.glue = glue;
	}

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		Optional.ofNullable(mergeOnto).ifPresent(wse -> element.setAnimatedOffset(wse.get()
			.getAnimatedOffset(), true));
		element.set(initialSelection);
		element.setVisible(true);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (remainingTicks > 0)
			return;
		Optional.ofNullable(mergeOnto).ifPresent(c -> element.mergeOnto(c.get()));
		//TODO
		//if (glue != null)
		//	SuperGlueItem.spawnParticles(scene.getWorld(), glue, fadeInFrom, true);
	}

	@Override
	protected Class<WorldSectionElement> getElementClass() {
		return WorldSectionElement.class;
	}

}
