package com.unityindustry.gameplay;

import com.unityindustry.registry.CoreComponents;
import com.unityindustry.registry.ModItems;
import com.unityindustry.rules.CoreRules;
import com.unityindustry.rules.CoreType;
import com.unityindustry.armor.UnityArmorMaterial;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The item component is authoritative. It remains on the armor when unequipped or dropped. */
public final class ChestplateCores {
    public static boolean validArmor(ItemStack stack) {
        return stack.is(ModItems.UNITY_CHESTPLATE);
    }

    public static List<CoreType> get(ItemStack armor) {
        if (!validArmor(armor)) return List.of();
        String raw = armor.getOrDefault(CoreComponents.INSTALLED_CORES, "");
        if (raw.isEmpty()) return List.of();
        List<CoreType> cores = new ArrayList<>();
        for (String value : raw.split(",")) {
            try {
                CoreType type = CoreType.valueOf(value.trim().toUpperCase(Locale.ROOT));
                if (!cores.contains(type) && CoreRules.slotsUsed(cores) + type.slots() <= CoreRules.CHESTPLATE_CAPACITY
                    && (type != CoreType.ULTRA || cores.isEmpty()) && !cores.contains(CoreType.ULTRA)) {
                    cores.add(type);
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid data is ignored; malformed items cannot bypass the three-slot cap.
            }
        }
        return List.copyOf(cores);
    }

    public static ItemStack with(ItemStack original, List<CoreType> cores) {
        if (!validArmor(original)) return ItemStack.EMPTY;
        ItemStack copy = original.copy();
        copy.set(CoreComponents.INSTALLED_CORES,
            String.join(",", cores.stream().map(Enum::name).toList()));
        Equippable previous = copy.get(DataComponents.EQUIPPABLE);
        if (previous != null) {
            String key = cores.isEmpty() ? "unity" :
                "unity_" + String.join("_", cores.stream().map(c -> c.name().toLowerCase(java.util.Locale.ROOT))
                    .sorted().toList());
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID,
                Identifier.fromNamespaceAndPath("unityindustry", key));
            copy.set(DataComponents.EQUIPPABLE, new Equippable(
                previous.slot(), previous.equipSound(), java.util.Optional.of(asset),
                previous.cameraOverlay(), previous.allowedEntities(), previous.dispensable(),
                previous.swappable(), previous.damageOnHurt(), previous.equipOnInteract(),
                previous.canBeSheared(), previous.shearingSound()));
        }
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.literal("Core: " + CoreRules.slotsUsed(cores) + "/3")
            .withStyle(ChatFormatting.GOLD));
        for (CoreType core : cores) {
            lore.add(Component.literal(core.name().replace('_', ' ') + " Core")
                .withStyle(core == CoreType.ULTRA ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
        }
        var skills = com.unityindustry.rules.SkillCatalog.active(cores);
        for (int i=0;i<skills.size();i++) {
            lore.add(Component.literal("[" + (i+1) + "] " + skills.get(i).action().label)
                .withStyle(ChatFormatting.YELLOW));
        }
        copy.set(DataComponents.LORE, new ItemLore(lore));
        return copy;
    }

    public static ItemStack itemFor(CoreType core) {
        return new ItemStack(switch (core) {
            case FIRE -> ModItems.FIRE_CORE;
            case FLYING -> ModItems.FLYING_CORE;
            case ORE_SCANNER -> ModItems.ORE_SCANNER_CORE;
            case EXCAVATION -> ModItems.EXCAVATION_CORE;
            case MAGNET -> ModItems.MAGNET_CORE;
            case PRESSURE -> ModItems.PRESSURE_CORE;
            case AGRICULTURE -> ModItems.AGRICULTURE_CORE;
            case REFINERY -> ModItems.REFINERY_CORE;
            case STORAGE -> ModItems.STORAGE_CORE;
            case MAINTENANCE -> ModItems.MAINTENANCE_CORE;
            case ULTRA -> ModItems.ULTRA_CORE;
        });
    }

    public static CoreType typeOf(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item item = stack.getItem();
        for (CoreType type : CoreType.values()) if (item == itemFor(type).getItem()) return type;
        return null;
    }

    private ChestplateCores() { }
}
