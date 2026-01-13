package net.createmod.ponder.foundation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Queue;

import net.minecraft.client.particle.ElderGuardianParticleGroup;
import net.minecraft.client.particle.ItemPickupParticleGroup;
import net.minecraft.client.particle.NoRenderParticleGroup;
import net.minecraft.client.particle.QuadParticleGroup;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleLimit;

public class PonderWorldParticles {
	private final ParticlesRenderState particleState = new ParticlesRenderState();
	private final Map<ParticleRenderType, ParticleGroup<?>> particles = Maps.newIdentityHashMap();
	private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
	private final Object2IntOpenHashMap<ParticleLimit> trackedParticleCounts = new Object2IntOpenHashMap<>();
	private final ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
	private final ParticleFeatureRenderer.ParticleBufferCache particleBufferCache = new ParticleFeatureRenderer.ParticleBufferCache();

	PonderLevel world;

	public PonderWorldParticles(PonderLevel world) {
		this.world = world;
	}

	public void addParticle(Particle effect) {
		Optional<ParticleLimit> optional = effect.getParticleLimit();
		if (optional.isPresent()) {
			if (this.hasSpaceInParticleLimit(optional.get())) {
				this.particlesToAdd.add(effect);
				this.updateCount(optional.get(), 1);
			}
		} else {
			this.particlesToAdd.add(effect);
		}
	}

	public void tick() {
		this.particles.forEach((renderType, particleGroup) -> particleGroup.tickParticles());

		Particle particle;
		if (!this.particlesToAdd.isEmpty()) {
			while ((particle = this.particlesToAdd.poll()) != null) {
				this.particles.computeIfAbsent(particle.getGroup(), this::createParticleGroup).add(particle);
			}
		}
	}

	//TODO basically lifted straight from ParticleEngine. Ideally would not duplicate code like this
	private ParticleGroup<?> createParticleGroup(ParticleRenderType renderType) {
		if (renderType == ParticleRenderType.ITEM_PICKUP) {
			return new ItemPickupParticleGroup(particleEngine);
		} else if (renderType == ParticleRenderType.ELDER_GUARDIANS) {
			return new ElderGuardianParticleGroup(particleEngine);
		} else {
			return (renderType == ParticleRenderType.NO_RENDER ? new NoRenderParticleGroup(particleEngine) : new QuadParticleGroup(particleEngine, renderType));
		}
	}

	public void renderParticles(PoseStack poseStack, SubmitNodeStorage queue, Camera camera,
								CameraRenderState cameraRenderState, float partialTick) {
		Minecraft mc = Minecraft.getInstance();

		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.mul(poseStack.last().pose());

		for (ParticleRenderType particlerendertype : ParticleEngine.RENDER_ORDER) {
			ParticleGroup<?> particleGroup = particles.get(particlerendertype);
			if (particleGroup != null && !particleGroup.isEmpty()) {
				particleState.add(particleGroup.extractRenderState(ParticlesFrustum.INSTANCE, camera, partialTick));
			}
		}

		particleState.submit(queue, cameraRenderState);

		for (SubmitNodeCollection collection : queue.getSubmitsPerOrder().values()) {
			List<ParticleGroupRenderer> renderers = collection.getParticleGroupRenderers();
			if (renderers.isEmpty()) {
				continue;
			}

			TextureManager textureManager = Minecraft.getInstance().getTextureManager();

			for (ParticleGroupRenderer renderer : renderers) {
				QuadParticleRenderState.PreparedBuffers preparedBuffers = renderer.prepare(particleBufferCache);
				if (preparedBuffers != null) {
					try (RenderPass renderPass = RenderSystem.getDevice()
						.createCommandEncoder()
						.createRenderPass(
							() -> "Particles - Ponder World",
							RenderSystem.outputColorTextureOverride,
							OptionalInt.empty(),
							RenderSystem.outputDepthTextureOverride,
							OptionalDouble.empty()
						)) {
						this.prepareRenderPass(renderPass);
						renderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager, false);
						renderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager, true);
					}
				}
			}
		}

		stack.popMatrix();
	}

	private void prepareRenderPass(RenderPass renderPass) {
		renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
		renderPass.setUniform("Fog", RenderSystem.getShaderFog());
		renderPass.bindTexture(
			"Sampler2", Minecraft.getInstance().gameRenderer.lightTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
		);
	}

	protected void updateCount(ParticleLimit limit, int count) {
		this.trackedParticleCounts.addTo(limit, count);
	}

	private boolean hasSpaceInParticleLimit(ParticleLimit limit) {
		return this.trackedParticleCounts.getInt(limit) < limit.limit();
	}

	public void clearEffects() {
		this.particles.clear();
		this.particlesToAdd.clear();
		this.trackedParticleCounts.clear();
	}

	public static class ParticlesFrustum extends Frustum {
		public static final Frustum INSTANCE = new ParticlesFrustum();

		private ParticlesFrustum() {
			super(new Matrix4f(), new Matrix4f());
		}

		@Override
		public boolean pointInFrustum(double x, double y, double z) {
			return true;
		}
	}
}
