package net.createmod.ponder.foundation.registration;

import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class DefaultSharedTextRegistrationHelper implements SharedTextRegistrationHelper {

	private final String namespace;
	private final PonderLocalization localization;

	public DefaultSharedTextRegistrationHelper(String namespace, PonderLocalization localization) {
		this.namespace = namespace;
		this.localization = localization;
	}

	@Override
	public void registerSharedText(String key, String en_us) {
		localization.registerShared(new ResourceLocation(namespace, key), en_us);
	}
}
