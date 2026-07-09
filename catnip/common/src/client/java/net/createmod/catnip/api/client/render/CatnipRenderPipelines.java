package net.createmod.catnip.api.client.render;

import java.util.function.Consumer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/// Additional [RenderPipeline]s and [Snippets][RenderPipeline.Snippet]s  provided by Catnip.
public class CatnipRenderPipelines {
	/// Normally in GUI rendering the primitive topology of a pipeline is ignored, and assumed to be quads.
	/// This snippet will indicate that a pipeline should use its actual draw mode.
	public static final RenderPipeline.Snippet USE_DRAW_MODE_IN_GUI_SNIPPET = ModClientHooksHelper.INSTANCE.useDrawModeInGui(RenderPipeline.builder()).buildSnippet();

	public static final RenderPipeline GUI_TRIANGLES = register(
		"gui_triangles",
		builder -> builder.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withCull(false), // TODO: this is set to false specifically for breadcrumb arrows, should this be its own pipeline?
		RenderPipelines.GUI_SNIPPET, USE_DRAW_MODE_IN_GUI_SNIPPET
	),

	POSITION_COLOR_STRIP = register(
		"position_color_strip",
		builder -> builder.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP),
		RenderPipelines.DEBUG_FILLED_SNIPPET
	),

	TRIANGLE_FAN = register(
		"triangle_fan",
		builder -> builder.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN),
		RenderPipelines.DEBUG_FILLED_SNIPPET
	);

	public static void init() {}

	private static RenderPipeline register(String name, Consumer<RenderPipeline.Builder> consumer, RenderPipeline.Snippet... snippets) {
		Identifier id = Catnip.id("pipeline/" + name);

		Builder builder = RenderPipeline.builder(snippets);
		consumer.accept(builder);
		RenderPipeline pipeline = builder.withLocation(id).build();

		RenderPipelineRegistry.INSTANCE.register(pipeline);
		return pipeline;
	}
}
