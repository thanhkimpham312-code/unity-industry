package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.menu.StationMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final MenuType<StationMenu> INDUSTRY = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "industry"),
            new MenuType<>((id, inventory) -> new StationMenu(id, inventory,
                    StationMenu.Station.INDUSTRY), FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<StationMenu> RESEARCH = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "research"),
            new MenuType<>((id, inventory) -> new StationMenu(id, inventory,
                    StationMenu.Station.RESEARCH), FeatureFlags.DEFAULT_FLAGS));
    public static void initialize() { }
    private ModMenus() { }
}
