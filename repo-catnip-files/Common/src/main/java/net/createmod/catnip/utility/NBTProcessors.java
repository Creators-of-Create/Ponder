package net.createmod.catnip.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public final class NBTProcessors {

	private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> processors = new HashMap<>();
	private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> survivalProcessors = new HashMap<>();

	public static synchronized void addProcessor(BlockEntityType<?> type, UnaryOperator<CompoundTag> processor) {
		processors.put(type, processor);
	}

	public static synchronized void addSurvivalProcessor(BlockEntityType<?> type,
		UnaryOperator<CompoundTag> processor) {
		survivalProcessors.put(type, processor);
	}

	public static UnaryOperator<CompoundTag> itemProcessor(String tagKey) {
		return data -> {
			CompoundTag compound = data.getCompound(tagKey);
			if (!compound.contains("tag", 10))
				return data;
			CompoundTag itemTag = compound.getCompound("tag");
			HashSet<String> keys = new HashSet<>(itemTag.getAllKeys());
			for (String key : keys)
				if (isUnsafeItemNBTKey(key))
					itemTag.remove(key);
			if (itemTag.isEmpty())
				compound.remove("tag");
			return data;
		};
	}

	public static ItemStack withUnsafeNBTDiscarded(ItemStack stack) {
		if (stack.getTag() == null)
			return stack;
		ItemStack copy = stack.copy();
		stack.getTag()
			.getAllKeys()
			.stream()
			.filter(NBTProcessors::isUnsafeItemNBTKey)
			.forEach(copy::removeTagKey);
		return copy;
	}

	public static boolean isUnsafeItemNBTKey(String name) {
		if (name.equals(EnchantedBookItem.TAG_STORED_ENCHANTMENTS))
			return false;
		if (name.equals("Enchantments"))
			return false;
		if (name.contains("Potion"))
			return false;
		if (name.contains("Damage"))
			return false;
		if (name.equals("display"))
			return false;
		return true;
	}

	public static boolean textComponentHasClickEvent(String json) {
		Component component = Component.Serializer.fromJson(json.isEmpty() ? "\"\"" : json);
		return component != null && component.getStyle().getClickEvent() != null;
	}

	private NBTProcessors() {}

	@Nullable
	public static CompoundTag process(BlockEntity blockEntity, @Nullable CompoundTag compound, boolean survival) {
		if (compound == null)
			return null;
		BlockEntityType<?> type = blockEntity.getType();
		if (survival && survivalProcessors.containsKey(type))
			compound = survivalProcessors.get(type)
				.apply(compound);
		if (compound != null && processors.containsKey(type))
			return processors.get(type)
				.apply(compound);
		if (blockEntity instanceof SpawnerBlockEntity)
			return compound;
		if (blockEntity.onlyOpCanSetNbt())
			return null;
		return compound;
	}

}
