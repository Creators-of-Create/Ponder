package net.createmod.catnip.api.client.placement;

import java.util.Objects;

public final class PlacementAssistConfig {
	private static Provider provider = Provider.DEFAULT;

	private PlacementAssistConfig() {
	}

	public static Provider get() {
		return provider;
	}

	public static void setProvider(Provider provider) {
		PlacementAssistConfig.provider = Objects.requireNonNull(provider);
	}

	public enum IndicatorSetting {
		TEXTURE, TRIANGLE, NONE
	}

	public interface Provider {
		Provider DEFAULT = new Provider() {
			@Override
			public IndicatorSetting placementIndicator() {
				return IndicatorSetting.TEXTURE;
			}

			@Override
			public float indicatorScale() {
				return 1;
			}
		};

		IndicatorSetting placementIndicator();

		float indicatorScale();
	}
}
