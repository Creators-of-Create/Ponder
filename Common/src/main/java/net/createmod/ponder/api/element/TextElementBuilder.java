package net.createmod.ponder.api.element;

import net.createmod.ponder.api.PonderPalette;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public interface TextElementBuilder {

	TextElementBuilder colored(PonderPalette color);

	TextElementBuilder pointAt(Vec3 vec);

	TextElementBuilder independent(int y);

	default TextElementBuilder independent() {
		return independent(0);
	}

	TextElementBuilder text(String defaultText);

	TextElementBuilder sharedText(ResourceLocation key);

	TextElementBuilder sharedText(String key);

	TextElementBuilder placeNearTarget();

	TextElementBuilder attachKeyFrame();
}
