package net.createmod.catnip.registration.holder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;

public class BaseHolder<T> extends Holder.Reference<T> {
	protected BaseHolder(HolderOwner<T> owner, ResourceKey<T> key) {
		super(Type.STAND_ALONE, owner, key, null);
	}

	public <V> boolean is(V value) {
		return value() == value;
	}

	public static <T> Holder<T> downcast(Holder<? extends T> holder) {
		//noinspection unchecked
		return (Holder<T>) holder;
	}

	public String getRegisteredNamePath() {
		return this.unwrapKey().map((resourceKey) -> resourceKey.location().getPath()).orElse("[unregistered]");
	}
}
