package net.createmod.catnip.config;

public class CClient extends ConfigBase {

	public final ConfigGroup client = group(0, "client", Comments.client);

	//placement assist group
	public final ConfigGroup placementAssist = group(1, "placementAssist",
			Comments.placementAssist);
	public final ConfigEnum<PlacementIndicatorSetting> placementIndicator = e(PlacementIndicatorSetting.TEXTURE, "indicatorType",
			Comments.placementIndicator);
	public final ConfigFloat indicatorScale = f(1.0f, 0f, "indicatorScale",
			Comments.indicatorScale);

	public enum PlacementIndicatorSetting {
		TEXTURE, TRIANGLE, NONE
	}

	@Override
	public String getName() {
		return "client";
	}

	private static class Comments {
		static String client = "Client-only settings";

		static String placementAssist = "Settings for the Placement Assist";
		static String[] placementIndicator = new String[]{
				"What indicator should be used when showing where the assisted placement ends up relative to your crosshair",
				"Choose 'NONE' to disable the Indicator altogether"
		};
		static String indicatorScale = "Change the size of the Indicator by this multiplier";

	}
}
