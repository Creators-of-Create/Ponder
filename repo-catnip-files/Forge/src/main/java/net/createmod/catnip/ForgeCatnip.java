package net.createmod.catnip;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.enums.CatnipConfig;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod(Catnip.MOD_ID)
public class ForgeCatnip {
    
    public ForgeCatnip() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        Catnip.LOGGER.info("Hello Forge world!");
        CommonClass.init();

        Catnip.init();

        registerConfigs(modLoadingContext);

        // Some code like events require special initialization from the
        // loader specific code.
        //MinecraftForge.EVENT_BUS.addListener(this::onItemTooltip);

    }

    private static void registerConfigs(ModLoadingContext modLoadingContext) {
        Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = CatnipConfig.registerConfigs();
        for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
            modLoadingContext.registerConfig(entry.getKey(), entry.getValue().specification);
        }
    }

    @Mod.EventBusSubscriber
    public static class Events {

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            CommonClass.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onLoad(ModConfigEvent.Loading event) {
            CatnipConfig.onLoad(event.getConfig());
        }

        @SubscribeEvent
        public static void onReload(ModConfigEvent.Reloading event) {
            CatnipConfig.onReload(event.getConfig());
        }

    }
}