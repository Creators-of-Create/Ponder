package net.createmod.catnip.api.client;

import net.minecraft.client.KeyMapping;

public class ConflictSafeKeyMapping extends KeyMapping {
	public ConflictSafeKeyMapping(String name, int key, String category) {
		super(name, key, Category.MISC);
	}
}
