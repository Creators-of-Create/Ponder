package net.createmod.catnip.platform;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.platform.services.RegisteredObjectsHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ForgeRegisteredObjectsHelper implements RegisteredObjectsHelper<IForgeRegistry> {

	@Override
	public <V> ResourceLocation getKeyOrThrow(IForgeRegistry registry, V value) {
		ResourceLocation key = null;
		if (value instanceof IForgeRegistryEntry<?> forgeEntry) {
			key = forgeEntry.getRegistryName();
		}

		if (key == null) {
			throw new IllegalArgumentException("Could not get key for value " + value + "!");
		}
		return key;
	}

	@Override
	public ResourceLocation getKeyOrThrow(Block value) {
		return getKeyOrThrow(ForgeRegistries.BLOCKS, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Item value) {
		return getKeyOrThrow(ForgeRegistries.ITEMS, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Fluid value) {
		return getKeyOrThrow(ForgeRegistries.FLUIDS, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(EntityType<?> value) {
		return getKeyOrThrow(ForgeRegistries.ENTITIES, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(BlockEntityType<?> value) {
		return getKeyOrThrow(ForgeRegistries.BLOCK_ENTITIES, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Potion value) {
		return getKeyOrThrow(ForgeRegistries.POTIONS, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(ParticleType<?> value) {
		return getKeyOrThrow(ForgeRegistries.PARTICLE_TYPES, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(RecipeSerializer<?> value) {
		return getKeyOrThrow(ForgeRegistries.RECIPE_SERIALIZERS, value);
	}

	@Nullable
	@Override
	public Item getItem(ResourceLocation location) {
		return ForgeRegistries.ITEMS.getValue(location);
	}

	@Nullable
	@Override
	public Block getBlock(ResourceLocation location) {
		return ForgeRegistries.BLOCKS.getValue(location);
	}
}
