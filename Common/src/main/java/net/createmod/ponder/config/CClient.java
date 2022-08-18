package net.createmod.ponder.config;

import net.createmod.catnip.config.ConfigBase;

public class CClient extends ConfigBase {

	public final ConfigGroup client = group(0, "client", Comments.client);

	public final ConfigBool comfyReading = b(false, "comfyReading",
			Comments.comfyReading);
	public final ConfigBool editingMode = b(false, "editingMode",
			Comments.editingMode);

	@Override
	public String getName() {
		return "client";
	}

	private static class Comments {
		static String client = "Client-only settings";

		static String comfyReading = "Slow down a ponder scene whenever there is text on screen.";
		static String editingMode = "Show additional info in the ponder view and reload scene scripts more frequently.";
	}
}
