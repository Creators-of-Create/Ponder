package net.createmod.catnip.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {

	@Invoker("create")
	static RenderType.CompositeRenderType catnip$create(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, RenderType.CompositeState compositeState) {
		throw new AssertionError("Mixin application failed!");
	}
}

