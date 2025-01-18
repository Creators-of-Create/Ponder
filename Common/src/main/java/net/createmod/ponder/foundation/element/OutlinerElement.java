package net.createmod.ponder.foundation.element;

import java.util.function.Function;

import net.createmod.catnip.outliner.Outline.OutlineParams;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.ponder.foundation.PonderScene;

public class OutlinerElement extends AnimatedSceneElementBase {

	private final Function<Outliner, OutlineParams> outlinerCall;
	private int overrideColor;

	public OutlinerElement(Function<Outliner, OutlineParams> outlinerCall) {
		this.outlinerCall = outlinerCall;
		this.overrideColor = -1;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (fade.getValue() < 1/16f)
			return;
		if (fade.getValue(0) > fade.getValue(1))
			return;
		OutlineParams params = outlinerCall.apply(scene.getOutliner());
		if (overrideColor != -1)
			params.colored(overrideColor);
	}

	public void setColor(int overrideColor) {
		this.overrideColor = overrideColor;
	}

}
