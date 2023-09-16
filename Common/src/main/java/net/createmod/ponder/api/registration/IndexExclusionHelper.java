package net.createmod.ponder.api.registration;

import java.util.function.Predicate;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public interface IndexExclusionHelper {

	IndexExclusionHelper exclude(ItemLike item);

	IndexExclusionHelper excludeItemVariants(Class<? extends Item> itemClazz, Item originalVariant);

	IndexExclusionHelper excludeBlockVariants(Class<? extends Block> blockClazz, Block originalVariant);

	IndexExclusionHelper exclude(Predicate<ItemLike> predicate);

}
