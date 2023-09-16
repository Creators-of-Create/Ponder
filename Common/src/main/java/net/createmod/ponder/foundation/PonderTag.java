package net.createmod.ponder.foundation;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.ponder.Ponder;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PonderTag implements ScreenElement {

	/**
	 * Highlight.ALL is a special PonderTag, used to indicate that all Tags
	 * for a certain Scene should be highlighted instead of selected single ones
	 */
	public static final class Highlight {
		public static final ResourceLocation ALL = Ponder.asResource("_all");
	}

	private final ResourceLocation id;
	@Nullable private final ResourceLocation textureIconLocation;
	private final ItemStack itemIcon;
	private final ItemStack mainItem;


	public PonderTag(ResourceLocation id, @Nullable ResourceLocation textureIconLocation, ItemStack itemIcon,
					 ItemStack mainItem) {
		this.id = id;
		this.textureIconLocation = textureIconLocation;
		this.itemIcon = itemIcon;
		this.mainItem = mainItem;
	}

	public ResourceLocation getId() {
		return id;
	}

	public ItemStack getMainItem() {
		return mainItem;
	}

	public String getTitle() {
		return PonderIndex.getLangAccess().getTagName(id);
	}

	public String getDescription() {
		return PonderIndex.getLangAccess().getTagDescription(id);
	}

	public void render(PoseStack ms, int x, int y) {
		ms.pushPose();
		ms.translate(x, y, 0);
		if (textureIconLocation != null) {
			RenderSystem.setShaderTexture(0, textureIconLocation);
			ms.scale(0.25f, 0.25f, 1);
			GuiComponent.blit(ms, 0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else if (!itemIcon.isEmpty()) {
			ms.translate(-2, -2, 0);
			ms.scale(1.25f, 1.25f, 1.25f);
			GuiGameElement.of(itemIcon)
				.render(ms);
		}
		ms.popPose();
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
