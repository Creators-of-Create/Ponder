package net.createmod.catnip.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.minecraft.client.KeyMapping;

/**
 * Marker class for a {@link KeyMapping} that is not registered to {@link KeyMapping#MAP} on Fabric.
 * On Forge, this is no difference between an instance of this class and a normal {@link KeyMapping}.
 * <p>
 * Normal keybind features such as {@link KeyMapping#isDown() isDown} and {@link KeyMapping#consumeClick() consumeClick}
 * will be unreliable. Instead, use {@link ModClientHooksHelper#isKeyPressed(KeyMapping) isKeyPressed}.
 * <p>
 * This is done to avoid conflicting with vanilla keybinds. Forge handles that case fine already.
 * <p>
 * This workaround will be fully obsolete on 1.21.9 or newer.
 */
// TODO: Port - Remove in 1.21.9+
@SuppressWarnings("JavadocReference")
public class ConflictSafeKeyMapping extends KeyMapping {
	public ConflictSafeKeyMapping(String description, int defaultKey, String category) {
		super(description, defaultKey, category);
	}

	public ConflictSafeKeyMapping(String description, InputConstants.Type type, int defaultKey, String category) {
		super(description, type, defaultKey, category);
	}
}
