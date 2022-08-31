package net.createmod.catnip;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class CommonClass {
    //TODO remove
    //
    // This method serves as an initialization hook for the mod. The vanilla
    // game has no mechanism to load tooltip listeners so this must be
    // invoked from a mod loader specific project like Forge or Fabric.
    public static void init() {
        
        Catnip.LOGGER.info("Hello from Common init!");
        Catnip.LOGGER.info("Diamond Item >> {}", Registry.ITEM.getKey(Items.DIAMOND));
    }
    
    // This method serves as a hook to modify item tooltips. The vanilla game
    // has no mechanism to load tooltip listeners so this must be registered
    // by a mod loader like Forge or Fabric.
    public static void onItemTooltip(ItemStack stack, TooltipFlag context, List<Component> tooltip) {
    
        if (!stack.isEmpty()) {
            
            final FoodProperties food = stack.getItem().getFoodProperties();
            
            if (food != null) {
    
                tooltip.add(new TextComponent("Nutrition: " + food.getNutrition()));
                tooltip.add(new TextComponent("Saturation: " + food.getSaturationModifier()));
            }
        }
    }
}