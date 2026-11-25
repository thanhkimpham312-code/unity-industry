package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.ModMachines;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Spawn eggs for 7 NEW hostile machines + 3 NEW bosses. All five old eggs are
 * already registered in ModItems; registering them here again would crash.
 * Must be invoked only AFTER ModMachines.TYPES is populated.
 */
public final class PrototypeMachineEggs {
    private PrototypeMachineEggs() { }
    public static final Map<String, Item> EGGS = new LinkedHashMap<>();

    public static void register() {
        for (var entry : ModMachines.TYPES.entrySet()) {
            String id = entry.getKey();
            String itemId = id + "_spawn_egg";
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, UnityIndustry.id(itemId));
            // In Minecraft 1.21.11, the entity type is set on the Item.Properties.
            // SpawnEggItem itself is constructed only with the properties.
            Item.Properties properties = new Item.Properties().spawnEgg(entry.getValue()).setId(key);
            Item egg = Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(properties));
            EGGS.put(id, egg);
        }
        ItemGroupEvents.modifyEntriesEvent(ModItems.TAB_KEY)
            .register(entries -> EGGS.values().forEach(egg -> entries.accept(egg)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
            .register(entries -> EGGS.values().forEach(egg -> entries.accept(egg)));
    }
}
