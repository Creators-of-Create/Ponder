package net.createmod.ponder.impl.client.scene;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Queue;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.impl.client.mixin.ParticleEngineAccessor;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.impl.client.mixin.FeatureRenderDispatcherAccessor;
import net.createmod.ponder.impl.client.mixin.ParticleFeatureRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer.ParticleBufferCache;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleLimit;

public class PonderWorldParticles {
	private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
	private final ParticlesRenderState particlesRenderState = new ParticlesRenderState();
	private final Map<ParticleRenderType, ParticleGroup<?>> particles = new IdentityHashMap<>();
	private final Queue<Particle> particlesToAdd = new ArrayDeque<>();
	private final Object2IntOpenHashMap<ParticleLimit> trackedParticleCounts = new Object2IntOpenHashMap<>();
	private final ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
	private final Queue<ParticleBufferCache> availableBuffers;
	private final List<ParticleBufferCache> usedBuffers;

	PonderLevel level;

	public PonderWorldParticles(PonderLevel level) {
		this.level = level;

		FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance()
			.gameRenderer
			.getFeatureRenderDispatcher();
		var particleFeatureRenderer = (ParticleFeatureRenderer & ParticleFeatureRendererAccessor) ((FeatureRenderDispatcherAccessor) featureRenderDispatcher)
			.ponder$getParticleFeatureRenderer();
		availableBuffers = particleFeatureRenderer.ponder$getAvailableBuffers();
		usedBuffers = particleFeatureRenderer.ponder$getUsedBuffers();
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
				ParticleEngineAccessor accessor = (ParticleEngineAccessor) this.particleEngine;
				this.particles.computeIfAbsent(particle.getGroup(), accessor::callCreateParticleGroup).add(particle);
			}
		}
	}

	public void extract(ParticlesRenderState particlesRenderState, Frustum frustum, Camera camera, float partialTickTime) {
		for (ParticleRenderType particleType : ParticleEngineAccessor.getRENDER_ORDER()) {
			ParticleGroup<?> particles = this.particles.get(particleType);
			if (particles != null && !particles.isEmpty()) {
				particlesRenderState.add(particles.extractRenderState(frustum, camera, partialTickTime));
			}
		}
	}

	public void renderParticles(PoseStack poseStack, Camera camera, CameraRenderState cameraRenderState, float partialTick) {
		extract(particlesRenderState, ParticlesFrustum.INSTANCE, camera, partialTick);

		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.mul(poseStack.last().pose());

		particlesRenderState.submit(submitNodeStorage, cameraRenderState);

		for (SubmitNodeCollection collection : submitNodeStorage.getSubmitsPerOrder().values()) {
			this.render(collection, false);
			this.render(collection, true);
		}

		particlesRenderState.reset();

		stack.popMatrix();
	}

	private void render(SubmitNodeCollection nodeCollection, boolean translucent) {
		List<ParticleGroupRenderer> renderers = nodeCollection.getParticleGroupRenderers();

		if (!renderers.isEmpty()) {
			GpuDevice device = RenderSystem.getDevice();
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager textureManager = minecraft.getTextureManager();
			RenderTarget mainTarget = minecraft.getMainRenderTarget();
			RenderTarget particleTarget = minecraft.levelRenderer.getParticlesTarget();
			GpuTextureView overrideColorView = RenderSystem.outputColorTextureOverride;
			GpuTextureView overrideDepthView = RenderSystem.outputDepthTextureOverride;

			for (SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer : renderers) {
				if (!particleGroupRenderer.isEmpty()) {
					ParticleFeatureRenderer.ParticleBufferCache buffer = this.availableBuffers.poll();
					if (buffer == null) {
						buffer = new ParticleFeatureRenderer.ParticleBufferCache();
					}

					this.usedBuffers.add(buffer);
					QuadParticleRenderState.PreparedBuffers prepared = particleGroupRenderer.prepare(buffer, translucent);
					if (prepared != null) {
						boolean useParticleTarget = particleTarget != null && translucent;
						GpuTextureView colorTextureView = overrideColorView != null ? overrideColorView : (useParticleTarget ? particleTarget.getColorTextureView() : mainTarget.getColorTextureView());
						GpuTextureView depthTextureView = overrideDepthView != null ? overrideDepthView : (useParticleTarget ? particleTarget.getDepthTextureView() : mainTarget.getDepthTextureView());

						try (RenderPass renderPass = device.createCommandEncoder()
							.createRenderPass(
								() -> "Ponder Particles - " + (translucent ? "Translucent" : "Solid"),
								colorTextureView,
								OptionalInt.empty(),
								depthTextureView,
								OptionalDouble.empty()
							)) {
							renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
							renderPass.setUniform("Fog", RenderSystem.getShaderFog());
							renderPass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
							particleGroupRenderer.render(prepared, buffer, renderPass, textureManager);
						}
					}
				}
			}
		}
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
