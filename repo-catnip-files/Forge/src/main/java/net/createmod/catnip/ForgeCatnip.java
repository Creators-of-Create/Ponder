package net.createmod.catnip;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.catnip.net.ForgeCatnipNetwork;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Catnip.MOD_ID)
public class ForgeCatnip {
    
    public ForgeCatnip() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        modEventBus.addListener(ForgeCatnip::init);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ForgeCatnipClient.onCtor(modEventBus, forgeEventBus));

        registerConfigs(modLoadingContext);
    }

    public static void init(final FMLCommonSetupEvent event) {
        Catnip.init();
        ForgeCatnipNetwork.register();
        ArgumentTypes.register(Catnip.asResource("config_path").toString(), ConfigPathArgument.class, new EmptyArgumentSerializer<>(ConfigPathArgument::path));
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
        public static void registerCommands(RegisterCommandsEvent event) {
            CatnipCommands.register(event.getDispatcher());
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