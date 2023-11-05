package net.createmod.ponder.foundation.instruction;

import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.OutlinerElement;
import net.createmod.ponder.foundation.element.TextWindowElement;

public class TextInstruction extends FadeInOutInstruction {

	private final TextWindowElement element;
	private OutlinerElement outline;

	public TextInstruction(TextWindowElement element, int duration) {
		super(duration);
		this.element = element;
	}

	public TextInstruction(TextWindowElement element, int duration, Selection selection) {
		this(element, duration);
		outline = new OutlinerElement(o -> selection.makeOutline(o)
			.lineWidth(1 / 16f));
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (outline != null)
			outline.setColor(element.getPalette().getColor());
	}

	@Override
	protected void show(PonderScene scene) {
		scene.addElement(element);
		element.setVisible(true);
		if (outline != null) {
			scene.addElement(outline);
			outline.setFade(1);
			outline.setVisible(true);
		}
	}

	@Override
	protected void hide(PonderScene scene) {
		element.setVisible(false);
		if (outline != null) {
			outline.setFade(0);
			outline.setVisible(false);
		}
	}

	@Override
	protected void applyFade(PonderScene scene, float fade) {
		element.setFade(fade);
	}

}
