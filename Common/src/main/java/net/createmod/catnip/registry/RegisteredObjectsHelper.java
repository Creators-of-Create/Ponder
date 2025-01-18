package net.createmod.catnip.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

public class RegisteredObjectsHelper {
	public static <V> ResourceLocation getKeyOrThrow(Registry<V> registry, V value) {
		ResourceLocation key = registry.getKey(value);
		if (key == null) {
			throw new IllegalArgumentException("Could not get key for value " + value + "!");
		}
		return key;
	}

	public static ResourceLocation getKeyOrThrow(Block value) {
		return getKeyOrThrow(BuiltInRegistries.BLOCK, value);
	}

	public static ResourceLocation getKeyOrThrow(Item value) {
		return getKeyOrThrow(BuiltInRegistries.ITEM, value);
	}

	public static ResourceLocation getKeyOrThrow(Fluid value) {
		return getKeyOrThrow(BuiltInRegistries.FLUID, value);
	}

	public static ResourceLocation getKeyOrThrow(EntityType<?> value) {
		return getKeyOrThrow(BuiltInRegistries.ENTITY_TYPE, value);
	}

	public static ResourceLocation getKeyOrThrow(BlockEntityType<?> value) {
		return getKeyOrThrow(BuiltInRegistries.BLOCK_ENTITY_TYPE, value);
	}

	public static ResourceLocation getKeyOrThrow(Potion value) {
		return getKeyOrThrow(BuiltInRegistries.POTION, value);
	}

	public static ResourceLocation getKeyOrThrow(ParticleType<?> value) {
		return getKeyOrThrow(BuiltInRegistries.PARTICLE_TYPE, value);
	}

	public static ResourceLocation getKeyOrThrow(RecipeSerializer<?> value) {
		return getKeyOrThrow(BuiltInRegistries.RECIPE_SERIALIZER, value);
	}

	public static Item getItem(ResourceLocation location) {
		return BuiltInRegistries.ITEM.get(location);
	}

	public static Block getBlock(ResourceLocation location) {
		return BuiltInRegistries.BLOCK.get(location);
	}

	@Nullable
	public static ItemLike getItemOrBlock(ResourceLocation location) {
		Item item = getItem(location);
		if (item != Items.AIR)
			return item;

		Block block = getBlock(location);
		if (block != Blocks.AIR)
			return block;

		return null;
	}

	public static ResourceLocation getKeyOrThrow(ItemLike itemLike) {
		if (itemLike instanceof Item item) {
			return getKeyOrThrow(item);
		} else if (itemLike instanceof Block block) {
			return getKeyOrThrow(block);
		}

		throw new IllegalArgumentException("Could not get key for itemLike " + itemLike + "!");
	}

}
