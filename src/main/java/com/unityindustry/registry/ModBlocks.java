package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.block.StationBlock;
import com.unityindustry.menu.StationMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registered world blocks. Working menu implementations follow in milestone 2. */
public final class ModBlocks {
    public static final Block INDUSTRY_CRAFTING_TABLE = register("industry_crafting_table", StationMenu.Station.INDUSTRY);
    public static final Block RESEARCH_DESK = register("research_desk", StationMenu.Station.RESEARCH);
    public static final Block IMPLANT_TABLE = register("implant_table");
    public static final Block REVERSE_IMPLANT_TABLE = register("reverse_implant_table");

    private static Block register(String path) { return register(path, null); }

    private static Block register(String path, StationMenu.Station station) {
        Identifier id = Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .strength(3.0f, 6.0f).sound(SoundType.METAL).setId(blockKey);
        Block block = station == null ? new Block(properties) : new StationBlock(properties, station);
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        return block;
    }

    public static void initialize() { /* triggers class loading */ }
    private ModBlocks() { }
}
