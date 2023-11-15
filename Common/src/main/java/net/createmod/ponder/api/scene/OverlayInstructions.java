package net.createmod.ponder.api.scene;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.InputElementBuilder;
import net.createmod.ponder.api.element.TextElementBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface OverlayInstructions {
	TextElementBuilder showText(int duration);

	TextElementBuilder showOutlineWithText(Selection selection, int duration);

	InputElementBuilder showControls(Vec3 sceneSpace, Pointing direction, int duration);

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
