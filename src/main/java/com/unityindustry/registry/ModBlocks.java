package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
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
    public static final Block INDUSTRY_CRAFTING_TABLE = register("industry_crafting_table");
    public static final Block RESEARCH_DESK = register("research_desk");
    public static final Block IMPLANT_TABLE = register("implant_table");
    public static final Block REVERSE_IMPLANT_TABLE = register("reverse_implant_table");

    private static Block register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Block block = new Block(BlockBehaviour.Properties.of()
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        return block;
    }

    public static void initialize() { /* triggers class loading */ }
    private ModBlocks() { }
}
