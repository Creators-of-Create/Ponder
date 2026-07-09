package net.createmod.catnip.api.client.gui;

import com.mojang.blaze3d.platform.Lighting.Entry;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface ILightingSettings {
	ILightingSettings LEVEL = setupFor(Entry.LEVEL);
	ILightingSettings ITEMS_FLAT = setupFor(Entry.ITEMS_FLAT);
	ILightingSettings ITEMS_3D = setupFor(Entry.ITEMS_3D);
	ILightingSettings ENTITY_IN_UI = setupFor(Entry.ENTITY_IN_UI);
	ILightingSettings PLAYER_SKIN = setupFor(Entry.PLAYER_SKIN);

	void apply();

	static void apply(Entry entry) {
		setupFor(entry).apply();
	}

	static ILightingSettings setupFor(Entry entry) {
		return () -> Minecraft.getInstance().gameRenderer.lighting().setupFor(entry);
	}
}
