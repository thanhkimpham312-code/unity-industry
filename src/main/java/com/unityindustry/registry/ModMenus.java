package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.menu.ImplantMenu;
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
        new MenuType<>((id, inv) -> new StationMenu(id, inv, StationMenu.Station.INDUSTRY), FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<StationMenu> RESEARCH = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "research"),
        new MenuType<>((id, inv) -> new StationMenu(id, inv, StationMenu.Station.RESEARCH), FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<ImplantMenu> IMPLANT = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "implant"),
        new MenuType<>((id, inv) -> new ImplantMenu(id, inv, ImplantMenu.Mode.IMPLANT), FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<ImplantMenu> REVERSE = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "reverse"),
        new MenuType<>((id, inv) -> new ImplantMenu(id, inv, ImplantMenu.Mode.REVERSE), FeatureFlags.DEFAULT_FLAGS));
    public static void initialize() { }
    private ModMenus() { }
}
