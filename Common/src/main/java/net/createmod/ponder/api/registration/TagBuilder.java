package net.createmod.ponder.api.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public interface TagBuilder {

	TagBuilder title(String title);

	TagBuilder description(String description);

	TagBuilder addToIndex();

	TagBuilder icon(ResourceLocation location);

	TagBuilder icon(String path);

	TagBuilder idAsIcon();

	TagBuilder item(ItemLike item, boolean useAsIcon, boolean useAsMainItem);

	default TagBuilder item(ItemLike item){
		return item(item, true, true);
	}

	void register();

}
