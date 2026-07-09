package net.createmod.catnip.api.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
	VertexConsumer getBuffer(RenderType type);

	static BufferSource immediate(ByteBufferBuilder fallbackBuffer) {
		return new BufferSource(Map.of(), fallbackBuffer);
	}

	static BufferSource immediateWithBuffers(SortedMap<RenderType, ByteBufferBuilder> fixedBuffers,
		ByteBufferBuilder fallbackBuffer) {
		return new BufferSource(fixedBuffers, fallbackBuffer);
	}

	class BufferSource implements MultiBufferSource {
		private final Map<RenderType, ByteBufferBuilder> fixedBuffers;
		private final ByteBufferBuilder fallbackBuffer;
		private final Map<RenderType, BufferBuilder> activeBuffers = new HashMap<>();

		public BufferSource(Map<RenderType, ByteBufferBuilder> fixedBuffers, ByteBufferBuilder fallbackBuffer) {
			this.fixedBuffers = fixedBuffers;
			this.fallbackBuffer = fallbackBuffer;
		}

		@Override
		public VertexConsumer getBuffer(RenderType type) {
			return activeBuffers.computeIfAbsent(type, this::createBuffer);
		}

		public void endBatch() {
			activeBuffers.clear();
		}

		public void endBatch(RenderType type) {
			activeBuffers.remove(type);
		}

		private BufferBuilder createBuffer(RenderType type) {
			ByteBufferBuilder buffer = fixedBuffers.getOrDefault(type, fallbackBuffer);
			return new BufferBuilder(buffer, type.primitiveTopology(), type.format());
		}
	}
}
