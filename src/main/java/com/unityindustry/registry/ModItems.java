package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.armor.UnityArmorMaterial;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;

/** All eleven approved specialist cores are registered, but skills are not active yet. */
public final class ModItems {
    public static final Item UNITY_INGOT = register("unity_ingot", new Item.Properties());
    public static final Item TOUGH_STICK = register("tough_stick", new Item.Properties());
    public static final Item PRIME_CORE = register("prime_core", new Item.Properties().stacksTo(16));

    public static final Item FIRE_CORE = register("fire_core", new Item.Properties().stacksTo(1));
    public static final Item FLYING_CORE = register("flying_core", new Item.Properties().stacksTo(1));
    public static final Item ORE_SCANNER_CORE = register("ore_scanner_core", new Item.Properties().stacksTo(1));
    public static final Item EXCAVATION_CORE = register("excavation_core", new Item.Properties().stacksTo(1));
    public static final Item MAGNET_CORE = register("magnet_core", new Item.Properties().stacksTo(1));
    public static final Item PRESSURE_CORE = register("pressure_core", new Item.Properties().stacksTo(1));
    public static final Item AGRICULTURE_CORE = register("agriculture_core", new Item.Properties().stacksTo(1));
    public static final Item REFINERY_CORE = register("refinery_core", new Item.Properties().stacksTo(1));
    public static final Item STORAGE_CORE = register("storage_core", new Item.Properties().stacksTo(1));
    public static final Item MAINTENANCE_CORE = register("maintenance_core", new Item.Properties().stacksTo(1));
    public static final Item ULTRA_CORE = register("ultra_core", new Item.Properties().stacksTo(1));

    public static final Item SCRAP_CRAWLER_SPAWN_EGG = register("scrap_crawler_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SCRAP_CRAWLER));

    public static final Item MINING_DRONE_SPAWN_EGG = register("mining_drone_spawn_egg", SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.MINING_DRONE));
    public static final Item CRYSTAL_BEETLE_SPAWN_EGG = register("crystal_beetle_spawn_egg", SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.CRYSTAL_BEETLE));
    public static final Item SECURITY_SENTINEL_SPAWN_EGG = register("security_sentinel_spawn_egg", SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.SECURITY_SENTINEL));
    public static final Item EXCAVATOR_MK1_SPAWN_EGG = register("excavator_mk1_spawn_egg", SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.EXCAVATOR_MK1));

    public static final Item UNITY_HELMET = armor("unity_helmet", ArmorType.HELMET);
    public static final Item UNITY_CHESTPLATE = armor("unity_chestplate", ArmorType.CHESTPLATE);
    public static final Item UNITY_LEGGINGS = armor("unity_leggings", ArmorType.LEGGINGS);
    public static final Item UNITY_BOOTS = armor("unity_boots", ArmorType.BOOTS);

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "main"));

    public static final CreativeModeTab TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.unityindustry"))
            .icon(() -> new ItemStack(PRIME_CORE))
            .displayItems((parameters, output) -> {
                output.accept(UNITY_INGOT);
                output.accept(TOUGH_STICK);
                output.accept(PRIME_CORE);
                output.accept(UNITY_HELMET);
                output.accept(UNITY_CHESTPLATE);
                output.accept(UNITY_LEGGINGS);
                output.accept(UNITY_BOOTS);
                output.accept(FIRE_CORE);
                output.accept(FLYING_CORE);
                output.accept(ORE_SCANNER_CORE);
                output.accept(EXCAVATION_CORE);
                output.accept(MAGNET_CORE);
                output.accept(PRESSURE_CORE);
                output.accept(AGRICULTURE_CORE);
                output.accept(REFINERY_CORE);
                output.accept(STORAGE_CORE);
                output.accept(MAINTENANCE_CORE);
                output.accept(ULTRA_CORE);
                output.accept(SCRAP_CRAWLER_SPAWN_EGG);
                output.accept(MINING_DRONE_SPAWN_EGG);
                output.accept(CRYSTAL_BEETLE_SPAWN_EGG);
                output.accept(SECURITY_SENTINEL_SPAWN_EGG);
                output.accept(EXCAVATOR_MK1_SPAWN_EGG);
                output.accept(ModBlocks.INDUSTRY_CRAFTING_TABLE);
                output.accept(ModBlocks.RESEARCH_DESK);
                output.accept(ModBlocks.IMPLANT_TABLE);
                output.accept(ModBlocks.REVERSE_IMPLANT_TABLE);
            }).build();

    private static Item armor(String path, ArmorType type) {
        return register(path, properties -> new Item(properties),
            new Item.Properties().humanoidArmor(UnityArmorMaterial.INSTANCE, type)
                .durability(type.getDurability(UnityArmorMaterial.BASE_DURABILITY)));
    }

    private static Item register(String path, Item.Properties properties) {
        return register(path, Item::new, properties);
    }

    private static <T extends Item> T register(String path,
            Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, path));
        T item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, TAB);
    }

    private ModItems() { }
}
