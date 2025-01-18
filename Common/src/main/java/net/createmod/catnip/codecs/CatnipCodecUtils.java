package net.createmod.catnip.codecs;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

public interface CatnipCodecUtils {
	static <T> Optional<T> decode(Codec<T> codec, Tag tag) {
		return decode(codec, NbtOps.INSTANCE, tag);
	}

	static <T> Optional<T> decode(Codec<T> codec, HolderLookup.Provider registries, Tag tag) {
		return decode(codec, RegistryOps.create(NbtOps.INSTANCE, registries), tag);
	}

	static <T, S> Optional<T> decode(Codec<T> codec, DynamicOps<S> ops, S s) {
		return Optional.ofNullable(codec.decode(ops, s).mapOrElse(Pair::getFirst, error -> null));
	}

	static <T> Optional<Tag> encode(Codec<T> codec, T t) {
		return encode(codec, NbtOps.INSTANCE, t);
	}

	static <T> Optional<Tag> encode(Codec<T> codec, HolderLookup.Provider registries, T t) {
		return encode(codec, RegistryOps.create(NbtOps.INSTANCE, registries), t);
	}

	static <T, S> Optional<S> encode(Codec<T> codec, DynamicOps<S> ops, T t) {
		return Optional.ofNullable(codec.encodeStart(ops, t).mapOrElse(tag -> tag, error -> null));
	}
}
