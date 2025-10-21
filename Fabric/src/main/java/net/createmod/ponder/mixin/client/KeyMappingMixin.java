package net.createmod.ponder.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.createmod.catnip.client.ConflictSafeKeyMapping;
import net.minecraft.client.KeyMapping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	@WrapWithCondition(
		method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
			ordinal = 1
		)
	)
	private boolean maybeDontAddToMapInitially(Map<?, ?> map, Object key, Object self) {
		return !(self instanceof ConflictSafeKeyMapping);
	}

	@WrapWithCondition(
		method = "resetMapping",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private static boolean maybeDontAddToMapOnReset(Map<?, ?> map, Object key, Object mapping) {
		return !(mapping instanceof ConflictSafeKeyMapping);
	}
}
