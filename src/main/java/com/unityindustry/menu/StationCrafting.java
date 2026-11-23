package com.unityindustry.menu;

import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Container;

/**
 * Server-side recipes for the dedicated stations. No minecraft:crafting recipe
 * is registered for these products: ordinary vanilla Crafting Tables cannot
 * craft them. Each check requires exact cell placement and no extra ingredients.
 */
public final class StationCrafting {
    public static ItemStack result(StationMenu.Station station, Container input) {
        if (station == StationMenu.Station.RESEARCH) return research(input);
        return industry(input);
    }

    private static ItemStack research(Container input) {
        Item a = item(input, 0), b = item(input, 1);
        if (a == ModItems.PRIME_CORE && b == ModItems.PRIME_CORE) return new ItemStack(ModItems.ULTRA_CORE);
        if (a != ModItems.PRIME_CORE && b != ModItems.PRIME_CORE) return ItemStack.EMPTY;
        Item signature = a == ModItems.PRIME_CORE ? b : a;
        if (signature == Items.MAGMA_CREAM) return new ItemStack(ModItems.FIRE_CORE);
        if (signature == Items.ELYTRA) return new ItemStack(ModItems.FLYING_CORE);
        if (signature == Items.ECHO_SHARD) return new ItemStack(ModItems.ORE_SCANNER_CORE);
        if (signature == Items.NETHERITE_PICKAXE) return new ItemStack(ModItems.EXCAVATION_CORE);
        if (signature == Items.LODESTONE) return new ItemStack(ModItems.MAGNET_CORE);
        if (signature == Items.CONDUIT) return new ItemStack(ModItems.PRESSURE_CORE);
        if (signature == Items.BONE_MEAL) return new ItemStack(ModItems.AGRICULTURE_CORE);
        if (signature == Items.BLAST_FURNACE) return new ItemStack(ModItems.REFINERY_CORE);
        if (signature == Items.SHULKER_SHELL) return new ItemStack(ModItems.STORAGE_CORE);
        if (signature == Items.ANVIL) return new ItemStack(ModItems.MAINTENANCE_CORE);
        return ItemStack.EMPTY;
    }

    private static ItemStack industry(Container input) {
        // Alloy: Copper + Iron + Gold can appear in any permutation in ANY horizontal row.
        for (int row = 0; row < 3; row++) {
            boolean foundCopper = false, foundIron = false, foundGold = false, valid = true;
            for (int i = 0; i < 9; i++) {
                Item current = item(input, i);
                if (i / 3 != row) { if (current != Items.AIR) valid = false; continue; }
                if (current == Items.COPPER_INGOT && !foundCopper) foundCopper = true;
                else if (current == Items.IRON_INGOT && !foundIron) foundIron = true;
                else if (current == Items.GOLD_INGOT && !foundGold) foundGold = true;
                else valid = false;
            }
            if (valid && foundCopper && foundIron && foundGold) return new ItemStack(ModItems.UNITY_INGOT);
        }
        if (shape(input, new Item[][] {
                {null,null,null}, {null,Items.OBSIDIAN,null}, {null,Items.OBSIDIAN,null}}))
            return new ItemStack(ModItems.TOUGH_STICK);
        if (shape(input, new Item[][] {
                {null,ModItems.UNITY_INGOT,null},
                {ModItems.UNITY_INGOT,Items.NETHER_STAR,ModItems.UNITY_INGOT},
                {null,ModItems.UNITY_INGOT,null}}))
            return new ItemStack(ModItems.PRIME_CORE);
        if (shape(input, new Item[][] {
                {Items.IRON_INGOT,Items.BOOK,Items.IRON_INGOT},
                {Items.REDSTONE,Items.OBSERVER,Items.REDSTONE},
                {Items.COPPER_INGOT,Items.GLASS,Items.COPPER_INGOT}}))
            return new ItemStack(ModBlocks.RESEARCH_DESK);
        if (shape(input, new Item[][] {
                {Items.IRON_INGOT,Items.PISTON,Items.IRON_INGOT},
                {Items.GLASS,Items.ARMOR_STAND,Items.GLASS},
                {Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT}}))
            return new ItemStack(ModBlocks.IMPLANT_TABLE);

        // Vanilla Diamond armor shapes, with Unity Ingots in place of diamonds.
        Item U = ModItems.UNITY_INGOT;
        if (shape(input, new Item[][] {{U,U,U},{U,null,U},{null,null,null}})
                || shape(input, new Item[][] {{null,null,null},{U,U,U},{U,null,U}}))
            return new ItemStack(ModItems.UNITY_HELMET);
        if (shape(input, new Item[][] {{U,null,U},{U,U,U},{U,U,U}}))
            return new ItemStack(ModItems.UNITY_CHESTPLATE);
        if (shape(input, new Item[][] {{U,U,U},{U,null,U},{U,null,U}}))
            return new ItemStack(ModItems.UNITY_LEGGINGS);
        if (shape(input, new Item[][] {{U,null,U},{U,null,U},{null,null,null}}))
            return new ItemStack(ModItems.UNITY_BOOTS);
        // Boots may also use the lower two rows, matching vanilla's shifted pattern.
        if (shape(input, new Item[][] {{null,null,null},{U,null,U},{U,null,U}}))
            return new ItemStack(ModItems.UNITY_BOOTS);
        return ItemStack.EMPTY;
    }

    private static boolean shape(Container input, Item[][] shape) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
            Item wanted = shape[row][col];
            Item actual = item(input, row * 3 + col);
            if (wanted == null ? actual != Items.AIR : actual != wanted) return false;
        }
        return true;
    }
    private static Item item(Container c, int i) {
        ItemStack stack = c.getItem(i);
        return stack.isEmpty() ? Items.AIR : stack.getItem();
    }
    private StationCrafting() { }
}
