package net.createmod.catnip.api.client.render;

import java.util.SortedMap;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Util;

public class DefaultSuperRenderTypeBuffer implements SuperRenderTypeBuffer {
	private static final DefaultSuperRenderTypeBuffer INSTANCE = new DefaultSuperRenderTypeBuffer();

	public static DefaultSuperRenderTypeBuffer getInstance() {
		return INSTANCE;
	}

	protected SuperRenderTypeBufferPhase earlyBuffer;
	protected SuperRenderTypeBufferPhase defaultBuffer;
	protected SuperRenderTypeBufferPhase lateBuffer;

	public DefaultSuperRenderTypeBuffer() {
		earlyBuffer = new SuperRenderTypeBufferPhase();
		defaultBuffer = new SuperRenderTypeBufferPhase();
		lateBuffer = new SuperRenderTypeBufferPhase();
	}

	@Override
	public VertexConsumer getEarlyBuffer(RenderType type) {
		return earlyBuffer.bufferSource.getBuffer(type);
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		return defaultBuffer.bufferSource.getBuffer(type);
	}

	@Override
	public VertexConsumer getLateBuffer(RenderType type) {
		return lateBuffer.bufferSource.getBuffer(type);
	}

	@Override
	public void draw() {
		earlyBuffer.bufferSource.endBatch();
		defaultBuffer.bufferSource.endBatch();
		lateBuffer.bufferSource.endBatch();
	}

	@Override
	public void draw(RenderType type) {
		earlyBuffer.bufferSource.endBatch(type);
		defaultBuffer.bufferSource.endBatch(type);
		lateBuffer.bufferSource.endBatch(type);
	}

	public static class SuperRenderTypeBufferPhase {
		// Visible clones from RenderBuffers
		private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
		private final SortedMap<RenderType, ByteBufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), map -> {
			// map.put(Sheets.solidBlockSheet(), fixedBufferPack.buffer(ChunkSectionLayer.SOLID));
			map.put(Sheets.cutoutBlockItemSheet(), fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
			map.put(Sheets.translucentItemSheet(), fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT));
			put(map, Sheets.translucentBlockItemSheet());
			// put(map, Sheets.shieldSheet());
			// put(map, Sheets.bedSheet());
			// put(map, Sheets.shulkerBoxSheet());
			// put(map, Sheets.signSheet());
			// put(map, Sheets.hangingSignSheet());
			// map.put(Sheets.chestSheet(), new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE));
			put(map, RenderTypes.armorEntityGlint());
			put(map, RenderTypes.glint());
			put(map, RenderTypes.glintTranslucent());
			put(map, RenderTypes.entityGlint());
			put(map, RenderTypes.waterMask());

			//extras
			put(map, PonderRenderTypes.outlineSolid());
		});
		private final BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, new ByteBufferBuilder(256));

		private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType type) {
			map.put(type, new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE));
		}
	}
}
