package net.createmod.catnip.api.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

// TODO - Everything here needs to be rethought with how codecs exist now and should be used everywhere they can
@Deprecated(forRemoval = true)
public class NBTHelper {
	public static void putMarker(CompoundTag nbt, String marker) {
		nbt.store(marker, Unit.CODEC, Unit.INSTANCE);
	}

	// Backwards compatible with 1.20
	public static BlockPos readBlockPos(CompoundTag nbt, String key) {
		Optional<BlockPos> pos = nbt.read(key, BlockPos.CODEC);
		if (pos.isPresent())
			return pos.get();
		CompoundTag oldTag = nbt.getCompoundOrEmpty(key);
		return new BlockPos(
			oldTag.getIntOr("X", 0),
			oldTag.getIntOr("Y", 0),
			oldTag.getIntOr("Z", 0)
		);
	}

	public static <T> ListTag writeCompoundList(Iterable<T> list, Function<T, CompoundTag> serializer) {
		ListTag listNBT = new ListTag();
		list.forEach(t -> {
			CompoundTag apply = serializer.apply(t);
			if (apply == null)
				return;
			listNBT.add(apply);
		});
		return listNBT;
	}

	public static <T> List<T> readCompoundList(ListTag listNBT, Function<CompoundTag, T> deserializer) {
		List<T> list = new ArrayList<>(listNBT.size());
		listNBT.forEach(inbt -> list.add(deserializer.apply((CompoundTag) inbt)));
		return list;
	}

	public static ListTag writeItemList(Iterable<ItemStack> list, HolderLookup.Provider registries) {
		ListTag listNBT = new ListTag();
		list.forEach(stack -> {
			if (stack.isEmpty())
				return;
			ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.ifPresent(listNBT::add);
		});
		return listNBT;
	}

	public static List<ItemStack> readItemList(ListTag listNBT, HolderLookup.Provider registries) {
		List<ItemStack> list = new ArrayList<>(listNBT.size());
		listNBT.forEach(tag -> ItemStack.OPTIONAL_CODEC
			.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.result()
			.filter(stack -> !stack.isEmpty())
			.ifPresent(list::add));
		return list;
	}

	public static void iterateCompoundList(ListTag listNBT, Consumer<CompoundTag> consumer) {
		listNBT.forEach(inbt -> consumer.accept((CompoundTag) inbt));
	}

	public static <T extends Enum<T>> void writeEnum(CompoundTag nbt, String key, T value) {
		nbt.putString(key, value.name());
	}

	public static <T extends Enum<T>> T readEnum(CompoundTag nbt, String key, Class<T> enumClass) {
		String value = nbt.getStringOr(key, "");
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException e) {
			T[] constants = enumClass.getEnumConstants();
			if (constants == null || constants.length == 0)
				throw e;
			return constants[0];
		}
	}

	public static void writeIdentifier(CompoundTag nbt, String key, Identifier value) {
		nbt.putString(key, value.toString());
	}

	public static Identifier readIdentifier(CompoundTag nbt, String key) {
		Identifier identifier = Identifier.tryParse(nbt.getStringOr(key, ""));
		return identifier == null ? Identifier.withDefaultNamespace("missing") : identifier;
	}

	public static ListTag writeAABB(AABB bb) {
		ListTag list = new ListTag();
		list.add(FloatTag.valueOf((float) bb.minX));
		list.add(FloatTag.valueOf((float) bb.minY));
		list.add(FloatTag.valueOf((float) bb.minZ));
		list.add(FloatTag.valueOf((float) bb.maxX));
		list.add(FloatTag.valueOf((float) bb.maxY));
		list.add(FloatTag.valueOf((float) bb.maxZ));
		return list;
	}

	public static AABB readAABB(ListTag list) {
		return new AABB(
			list.getFloatOr(0, 0),
			list.getFloatOr(1, 0),
			list.getFloatOr(2, 0),
			list.getFloatOr(3, 0),
			list.getFloatOr(4, 0),
			list.getFloatOr(5, 0)
		);
	}

	public static Tag getINBT(CompoundTag nbt, String id) {
		Tag inbt = nbt.get(id);
		if (inbt != null)
			return inbt;
		return new CompoundTag();
	}

	public static CompoundTag intToCompound(int i) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putInt("V", i);
		return compoundTag;
	}

	public static int intFromCompound(CompoundTag compoundTag) {
		return compoundTag.getIntOr("V", 0);
	}
}
