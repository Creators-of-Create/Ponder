package net.createmod.ponder.api.scene;

import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.TextWindowElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface OverlayInstructions {
	TextWindowElement.Builder showText(int duration);

	TextWindowElement.Builder showSelectionWithText(Selection selection, int duration);

	void showControls(InputWindowElement element, int duration);

	void chaseBoundingBoxOutline(PonderPalette color, Object slot, AABB boundingBox, int duration);

	void showCenteredScrollInput(BlockPos pos, Direction side, int duration);

	void showScrollInput(Vec3 location, Direction side, int duration);

	void showRepeaterScrollInput(BlockPos pos, int duration);

	void showFilterSlotInput(Vec3 location, int duration);

	void showFilterSlotInput(Vec3 location, Direction side, int duration);

	void showLine(PonderPalette color, Vec3 start, Vec3 end, int duration);

	void showBigLine(PonderPalette color, Vec3 start, Vec3 end, int duration);

	void showOutline(PonderPalette color, Object slot, Selection selection, int duration);
}
