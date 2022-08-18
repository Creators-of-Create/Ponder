package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Ponder.MOD_ID)
public class ForgePonder {

	public ForgePonder() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		Ponder.init();

		registerConfigs(modLoadingContext);
	}

	private static void registerConfigs(ModLoadingContext modLoadingContext) {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			modLoadingContext.registerConfig(entry.getKey(), entry.getValue().specification);
		}
	}

	@Mod.EventBusSubscriber
	public static class Events {

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event) {
			PonderTooltipHandler.addToTooltip(event.getToolTip(), event.getItemStack());
		}

	}

}
