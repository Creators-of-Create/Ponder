package net.createmod.ponder.foundation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.ponder.Ponder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;

public class PonderTag implements ScreenElement {

	/**
	 * Highlight.ALL is a special PonderTag, used to indicate that all Tags
	 * for a certain Scene should be highlighted instead of selected single ones
	 */
	public static final class Highlight {
		public static final PonderTag ALL = create("_all");
	}

	private final ResourceLocation id;
	@Nullable private ResourceLocation icon;
	private ItemStack itemIcon = ItemStack.EMPTY;
	private ItemStack mainItem = ItemStack.EMPTY;

	public PonderTag(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation getId() {
		return id;
	}

	public ItemStack getMainItem() {
		return mainItem;
	}

	public String getTitle() {
		return PonderLocalization.getTag(id);
	}

	public String getDescription() {
		return PonderLocalization.getTagDescription(id);
	}

	// Builder

	public PonderTag defaultLang(String title, String description) {
		PonderLocalization.registerTag(id, title, description);
		return this;
	}

	public PonderTag addToIndex() {
		PonderRegistry.TAGS.listTag(this);
		return this;
	}

	public PonderTag icon(ResourceLocation location) {
		this.icon = new ResourceLocation(location.getNamespace(), "textures/ponder/tag/" + location.getPath() + ".png");
		return this;
	}

	public PonderTag icon(String location) {
		this.icon = new ResourceLocation(id.getNamespace(), "textures/ponder/tag/" + location + ".png");
		return this;
	}

	public PonderTag idAsIcon() {
		return icon(id);
	}

	public PonderTag item(ItemLike item, boolean useAsIcon, boolean useAsMainItem) {
		if (useAsIcon)
			this.itemIcon = new ItemStack(item);
		if (useAsMainItem)
			this.mainItem = new ItemStack(item);
		return this;
	}

	public PonderTag item(ItemLike item) {
		return this.item(item, true, true);
	}

	public void render(GuiGraphics graphics, int x, int y) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x, y, 0);
		if (icon != null) {
			//RenderSystem.setShaderTexture(0, icon);
			poseStack.scale(0.25f, 0.25f, 1);
			graphics.blit(icon, 0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else if (!itemIcon.isEmpty()) {
			poseStack.translate(-2, -2, 0);
			poseStack.scale(1.25f, 1.25f, 1.25f);
			GuiGameElement.of(itemIcon)
				.render(graphics);
		}
		poseStack.popPose();
	}

	private static PonderTag create(String id) {
		return create(Ponder.MOD_ID, id);
	}

	protected static PonderTag create(String namespace, String id) {
		return new PonderTag(new ResourceLocation(namespace, id));
	}

}
