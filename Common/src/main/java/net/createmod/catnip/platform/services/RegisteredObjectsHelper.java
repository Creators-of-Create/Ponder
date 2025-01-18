package net.createmod.catnip.platform.services;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

public interface RegisteredObjectsHelper<R> {

	<V> ResourceLocation getKeyOrThrow(R registry, V value);

	ResourceLocation getKeyOrThrow(Block value);

	ResourceLocation getKeyOrThrow(Item value);

	ResourceLocation getKeyOrThrow(Fluid value);

	ResourceLocation getKeyOrThrow(EntityType<?> value);

	ResourceLocation getKeyOrThrow(BlockEntityType<?> value);

	ResourceLocation getKeyOrThrow(Potion value);

	ResourceLocation getKeyOrThrow(ParticleType<?> value);

	ResourceLocation getKeyOrThrow(RecipeSerializer<?> value);

	@Nullable
	Item getItem(ResourceLocation location);

	@Nullable
	Block getBlock(ResourceLocation location);

	@Nullable
	default ItemLike getItemOrBlock(ResourceLocation location) {
		Item item = getItem(location);
		if (item != null)
			return item;

		return getBlock(location);
	}

	default ResourceLocation getKeyOrThrow(ItemLike itemLike) {
		if (itemLike instanceof Item item) {
			return getKeyOrThrow(item);
		} else if (itemLike instanceof Block block) {
			return getKeyOrThrow(block);
		}

		throw new IllegalArgumentException("Could not get key for itemLike " + itemLike + "!");
	}

}
