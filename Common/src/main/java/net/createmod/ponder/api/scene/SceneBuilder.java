package net.createmod.ponder.api.scene;

import java.util.function.Consumer;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;

public interface SceneBuilder {
	/**
	 * Ponder's toolkit for showing information on top of the scene world, such as
	 * highlighted bounding boxes, texts, icons and keybindings.
	 */
	OverlayInstructions overlay();

	/**
	 * Instructions for manipulating the schematic and its currently visible areas.
	 * Allows to show, hide and modify blocks as the scene plays out.
	 */
	WorldInstructions world();

	/**
	 * Additional tools for debugging ponder and bypassing the facade
	 */
	DebugInstructions debug();

	/**
	 * Special effects to embellish and communicate with
	 */
	EffectInstructions effects();

	/**
	 * Random other instructions that might come in handy
	 */
	SpecialInstructions special();

	PonderScene getScene();

	/**
	 * Assign a unique translation key, as well as the standard english translation
	 * for this scene's title using this method, anywhere inside the program
	 * function.
	 *
	 * @param sceneId unique ID for this scene, used as a prefix for translation entries
	 * @param title   title for this scene, in english
	 */
	void title(String sceneId, String title);

	/**
	 * Communicates to the ponder UI which parts of the schematic make up the base
	 * horizontally. Use of this is encouraged whenever there are components outside
	 * the base plate. <br>
	 * As a result, showBasePlate() will only show the configured size, and the
	 * scene's scaling inside the UI will be consistent with its base size.
	 *
	 * @param xOffset       Block spaces between the base plate and the schematic
	 *                      boundary on the Western side.
	 * @param zOffset       Block spaces between the base plate and the schematic
	 *                      boundary on the Northern side.
	 * @param basePlateSize Length in blocks of the base plate itself. Ponder
	 *                      assumes it to be square
	 */
	void configureBasePlate(int xOffset, int zOffset, int basePlateSize);

	/**
	 * Use this in case you are not happy with the scale of the scene relative to
	 * the overlay
	 *
	 * @param factor {@literal >}1 will make the scene appear larger, smaller
	 *               otherwise
	 */
	void scaleSceneView(float factor);

	/**
	 * Use this to disable the base plate's shadow for this scene
	 */
	void removeShadow();

	/**
	 * Use this in case you are not happy with the vertical alignment of the scene
	 * relative to the overlay
	 *
	 * @param yOffset {@literal >}0 moves the scene up, down otherwise
	 */
	void setSceneOffsetY(float yOffset);

	/**
	 * Fade the layer of blocks into the scene ponder assumes to be the base plate
	 * of the schematic's structure. Makes for a nice opener
	 */
	void showBasePlate();

	/**
	 * Adds an instruction to the scene. It is recommended to only use this method
	 * if another method in this class or its subclasses does not already allow
	 * adding a certain instruction.
	 */
	void addInstruction(PonderInstruction instruction);

	/**
	 * Adds a simple instruction to the scene. It is recommended to only use this
	 * method if another method in this class or its subclasses does not already
	 * allow adding a certain instruction.
	 */
	void addInstruction(Consumer<PonderScene> callback);

	/**
	 * Before running the upcoming instructions, wait for a duration to let previous
	 * actions play out. <br>
	 * Idle does not stall any animations, only schedules a time gap between
	 * instructions.
	 *
	 * @param ticks Ticks to wait for
	 */
	void idle(int ticks);

	/**
	 * Before running the upcoming instructions, wait for a duration to let previous
	 * actions play out. <br>
	 * Idle does not stall any animations, only schedules a time gap between
	 * instructions.
	 *
	 * @param seconds Seconds to wait for
	 */
	void idleSeconds(int seconds);

	/**
	 * Once the scene reaches this instruction in the timeline, mark it as
	 * "finished". This happens automatically when the end of a storyboard is
	 * reached, but can be desirable to do earlier, in order to bypass the wait for
	 * any residual text windows to time out. <br>
	 * So far this event only affects the "next scene" button in the UI to flash.
	 */
	void markAsFinished();

	/**
	 * Control whether the "next scene" popup should be displayed for this scene
	 * once the previous scene has finished running all of its instructions.
	 */
	void setNextUpEnabled(boolean isEnabled);

	/**
	 * Pans the scene's camera view around the vertical axis by the given amount
	 */
	void rotateCameraY(float degrees);

	/**
	 * Adds a Key Frame at the end of the last delay() instruction for the users to
	 * skip to
	 */
	void addKeyframe();

	/**
	 * Adds a Key Frame a couple ticks after the last delay() instruction for the
	 * users to skip to
	 */
	void addLazyKeyframe();
}
