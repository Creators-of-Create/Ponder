package net.createmod.ponder.impl.client.scene;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.impl.client.mixin.ParticleEngineAccessor;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.core.particles.ParticleLimit;

public class PonderWorldParticles {
	private final ParticlesRenderState particleState = new ParticlesRenderState();
	private final Map<ParticleRenderType, ParticleGroup<?>> particles = Maps.newIdentityHashMap();
	private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
	private final Object2IntOpenHashMap<ParticleLimit> trackedParticleCounts = new Object2IntOpenHashMap<>();
	private final ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;

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
				ParticleEngineAccessor accessor = (ParticleEngineAccessor) this.particleEngine;
				this.particles.computeIfAbsent(particle.getGroup(), accessor::callCreateParticleGroup).add(particle);
			}
		}
	}

	public void renderParticles(PoseStack poseStack, SubmitNodeCollector queue, Camera camera, CameraRenderState cameraRenderState, float partialTick) {
		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.mul(poseStack.last().pose());

		for (ParticleRenderType renderType : ParticleEngineAccessor.getRENDER_ORDER()) {
			ParticleGroup<?> group = particles.get(renderType);
			if (group != null && !group.isEmpty()) {
				particleState.add(group.extractRenderState(ParticlesFrustum.INSTANCE, camera, partialTick));
			}
		}

		particleState.submit(queue, cameraRenderState);
		particleState.reset();

		stack.popMatrix();
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
