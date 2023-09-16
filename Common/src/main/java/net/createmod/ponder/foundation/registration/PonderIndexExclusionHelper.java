package net.createmod.ponder.foundation.registration;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class PonderIndexExclusionHelper implements IndexExclusionHelper {

	private final Stream.Builder<Predicate<ItemLike>> exclusions = Stream.builder();

	public static Stream<Predicate<ItemLike>> pluginToExclusions(PonderPlugin plugin) {
		PonderIndexExclusionHelper helper = new PonderIndexExclusionHelper();
		plugin.indexExclusions(helper);
		return helper.getExclusions();
	}

	public Stream<Predicate<ItemLike>> getExclusions() {
		return exclusions.build();
	}

	@Override
	public IndexExclusionHelper exclude(ItemLike item) {
		exclusions.add(itemLike -> itemLike.asItem() == item.asItem());
		return this;
	}

	@Override
	public IndexExclusionHelper excludeItemVariants(Class<? extends Item> itemClazz, Item originalVariant) {
		exclusions.add(itemLike -> {
			if (!(itemClazz.isInstance(itemLike)))
				return false;

			return itemLike.asItem() != originalVariant.asItem();
		});
		return this;
	}

	@Override
	public IndexExclusionHelper excludeBlockVariants(Class<? extends Block> blockClazz, Block originalVariant) {
		exclusions.add(itemLike -> {
			if (!(itemLike instanceof BlockItem blockItem))
				return false;

			Block block = blockItem.getBlock();
			if (!(blockClazz.isInstance(block)))
				return false;

			return block.asItem() != originalVariant.asItem();
		});
		return this;
	}

	@Override
	public IndexExclusionHelper exclude(Predicate<ItemLike> predicate) {
		exclusions.add(predicate);
		return this;
	}
}
