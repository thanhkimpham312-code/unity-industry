package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.block.StationBlock;
import com.unityindustry.block.WideStationBlock;
import com.unityindustry.menu.ImplantMenu;
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

/** Industry is one block; all three other tables are two physical blocks. */
public final class ModBlocks {
    public static final Block INDUSTRY_CRAFTING_TABLE = register("industry_crafting_table", StationMenu.Station.INDUSTRY, null, false);
    public static final Block RESEARCH_DESK = register("research_desk", StationMenu.Station.RESEARCH, null, true);
    public static final Block IMPLANT_TABLE = register("implant_table", null, ImplantMenu.Mode.IMPLANT, true);
    public static final Block REVERSE_IMPLANT_TABLE = register("reverse_implant_table", null, ImplantMenu.Mode.REVERSE, true);

    private static Block register(String name, StationMenu.Station station, ImplantMenu.Mode mode, boolean wide) {
        Identifier id = Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .strength(3f, 6f).sound(SoundType.METAL).setId(key);
        Block block = wide ? new WideStationBlock(properties, station, mode)
            : new StationBlock(properties, station);
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
            new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        return block;
    }
    public static void initialize() { }
    private ModBlocks() { }
}
