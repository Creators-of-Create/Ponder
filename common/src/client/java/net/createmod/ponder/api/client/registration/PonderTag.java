package net.createmod.ponder.api.client.registration;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.element.ScreenElement;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PonderTag implements ScreenElement {
	/**
	 * Highlight.ALL is a special PonderTag, used to indicate that all Tags
	 * for a certain Scene should be highlighted instead of selected single ones
	 */
	public static final class Highlight {
		public static final Identifier ALL = Ponder.id("_all");
	}

	private final Identifier id;
	@Nullable
	private final Identifier textureIconLocation;
	@Nullable
	private final ItemLike itemIconSource;
	@Nullable
	private final ItemLike mainItemSource;
	private final ItemStack itemIcon;
	private final ItemStack mainItem;


	public PonderTag(Identifier id, @Nullable Identifier textureIconLocation, ItemStack itemIcon,
					 ItemStack mainItem) {
		this.id = id;
		this.textureIconLocation = textureIconLocation;
		this.itemIconSource = null;
		this.mainItemSource = null;
		this.itemIcon = itemIcon;
		this.mainItem = mainItem;
	}

	public PonderTag(Identifier id, @Nullable Identifier textureIconLocation, @Nullable ItemLike itemIcon,
					 @Nullable ItemLike mainItem) {
		this.id = id;
		this.textureIconLocation = textureIconLocation;
		this.itemIconSource = itemIcon;
		this.mainItemSource = mainItem;
		this.itemIcon = ItemStack.EMPTY;
		this.mainItem = ItemStack.EMPTY;
	}

	public Identifier getId() {
		return id;
	}

	public ItemStack getMainItem() {
		if (mainItemSource != null)
			return new ItemStack(mainItemSource);
		return mainItem;
	}

	public String getTitle() {
		return PonderIndex.getLangAccess().getTagName(id);
	}

	public String getDescription() {
		return PonderIndex.getLangAccess().getTagDescription(id);
	}

	public void render(GuiGraphicsExtractor graphics, int x, int y) {
		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(x, y);
		if (textureIconLocation != null) {
			poseStack.scale(0.25f, 0.25f);
			graphics.blit(RenderPipelines.GUI_TEXTURED, textureIconLocation, 0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else {
			ItemStack stack = itemIconSource != null ? new ItemStack(itemIconSource) : itemIcon;
			if (stack.isEmpty())
				return;
			GuiGameElement.of(stack)
				.scale(1.25f)
				.at(-2, -2)
				.submit(graphics);
		}
		poseStack.popMatrix();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (!(other instanceof PonderTag otherTag))
			return false;

		return getId().equals(otherTag.getId());
	}
}
