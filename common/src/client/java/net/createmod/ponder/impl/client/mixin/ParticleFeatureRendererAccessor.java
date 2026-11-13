package net.createmod.ponder.impl.client.mixin;

import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer.ParticleBufferCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Queue;

@Mixin(ParticleFeatureRenderer.class)
public interface ParticleFeatureRendererAccessor {
	@Accessor("availableBuffers")
	Queue<ParticleBufferCache> ponder$getAvailableBuffers();

	@Accessor("usedBuffers")
	List<ParticleBufferCache> ponder$getUsedBuffers();
}
