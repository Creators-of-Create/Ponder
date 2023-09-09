package net.createmod.ponder.foundation;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

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
	private String defaultTitle = "";
	private String defaultDescription = "";

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
		return PonderIndex.getLangAccess().getTagName(id);
	}

	public String getDescription() {
		return PonderIndex.getLangAccess().getTagDescription(id);
	}

	// Builder

	public PonderTag defaultLang(String title, String description) {
		this.defaultTitle = title;
		this.defaultDescription = description;
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

	public void render(PoseStack ms, int x, int y) {
		ms.pushPose();
		ms.translate(x, y, 0);
		if (icon != null) {
			RenderSystem.setShaderTexture(0, icon);
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

	public void registerLang(PonderLocalization localization) {
		localization.registerTag(id, defaultTitle, defaultDescription);
	}

	private static PonderTag create(String id) {
		return create(Ponder.MOD_ID, id);
	}

	protected static PonderTag create(String namespace, String id) {
		return new PonderTag(new ResourceLocation(namespace, id));
	}

}
