package com.unityindustry.armor;

import com.unityindustry.UnityIndustry;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/** Diamond defensive values, independent Unity appearance, no Unity Sword. */
public final class UnityArmorMaterial {
    public static final int BASE_DURABILITY = 33;
    private static final Identifier ID = Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "unity");
    public static final ResourceKey<EquipmentAsset> ASSET_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ID);
    public static final TagKey<Item> REPAIR_ITEMS = TagKey.create(
            BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "repairs_unity_armor"));

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(ArmorType.HELMET, 3,
                   ArmorType.CHESTPLATE, 8,
                   ArmorType.LEGGINGS, 6,
                   ArmorType.BOOTS, 3),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F,
            0.0F,
            REPAIR_ITEMS,
            ASSET_KEY
    );

    private UnityArmorMaterial() { }
}
