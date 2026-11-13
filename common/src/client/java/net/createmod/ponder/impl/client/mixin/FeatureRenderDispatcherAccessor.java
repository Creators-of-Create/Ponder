package net.createmod.ponder.impl.client.mixin;

import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FeatureRenderDispatcher.class)
public interface FeatureRenderDispatcherAccessor {
	@Accessor("particleFeatureRenderer")
	ParticleFeatureRenderer ponder$getParticleFeatureRenderer();
}
