package net.createmod.ponder.enums;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.createmod.ponder.Ponder;
import net.minecraft.client.KeyMapping;

public enum PonderKeybinds {

	PONDER("ponder", GLFW.GLFW_KEY_W)

	;

	private KeyMapping keyMapping;
	private String description;
	private int key;
	private boolean modifiable;

	private PonderKeybinds(String description, int defaultKey) {
		this.description = Ponder.MOD_ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
	}

	public static void register(Consumer<KeyMapping> registrationCallback) {
		for (PonderKeybinds key : values()) {
			key.keyMapping = new KeyMapping(key.description, key.key, Ponder.MOD_NAME);
			if (!key.modifiable)
				continue;

			registrationCallback.accept(key.keyMapping);
		}
	}

	public KeyMapping getKeybind() {
		return keyMapping;
	}

}
