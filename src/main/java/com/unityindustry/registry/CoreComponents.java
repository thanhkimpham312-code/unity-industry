package com.unityindustry.registry;

import com.mojang.serialization.Codec;
import com.unityindustry.UnityIndustry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

/** Persistent and networked list of installed cores, serialized on the chestplate ItemStack. */
public final class CoreComponents {
    public static final DataComponentType<String> INSTALLED_CORES = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "installed_cores"),
        DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build()
    );
    public static void initialize() { }
    private CoreComponents() { }
}
