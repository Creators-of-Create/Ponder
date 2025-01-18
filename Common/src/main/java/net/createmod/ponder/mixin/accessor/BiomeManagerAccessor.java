package net.createmod.ponder.mixin.accessor;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeManager.class)
public interface BiomeManagerAccessor {
	@Accessor("biomeZoomSeed")
	long catnip$getBiomeZoomSeed();
}
