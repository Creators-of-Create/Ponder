package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.RegisteredObjectsHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

public class FabricRegisteredObjectsHelper implements RegisteredObjectsHelper<Registry> {

	public <V> ResourceLocation getKeyOrThrow(Registry registry, V value) {
		ResourceLocation key = registry.getKey(value);
		if (key == null) {
			throw new IllegalArgumentException("Could not get key for value " + value + "!");
		}
		return key;
	}

	@Override
	public ResourceLocation getKeyOrThrow(Block value) {
		return getKeyOrThrow(Registry.BLOCK, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Item value) {
		return getKeyOrThrow(Registry.ITEM, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Fluid value) {
		return getKeyOrThrow(Registry.FLUID, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(EntityType<?> value) {
		return getKeyOrThrow(Registry.ENTITY_TYPE, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(BlockEntityType<?> value) {
		return getKeyOrThrow(Registry.BLOCK_ENTITY_TYPE, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(Potion value) {
		return getKeyOrThrow(Registry.POTION, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(ParticleType<?> value) {
		return getKeyOrThrow(Registry.PARTICLE_TYPE, value);
	}

	@Override
	public ResourceLocation getKeyOrThrow(RecipeSerializer<?> value) {
		return getKeyOrThrow(Registry.RECIPE_SERIALIZER, value);
	}
}
