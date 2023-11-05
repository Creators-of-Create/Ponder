package net.createmod.ponder.api.element;

import net.minecraft.world.phys.Vec3;

public interface AnimatedSceneElement extends PonderSceneElement {
	void forceApplyFade(float fade);

	void setFade(float fade);

	void setFadeVec(Vec3 fadeVec);
}
