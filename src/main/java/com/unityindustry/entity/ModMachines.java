package com.unityindustry.entity;

import com.unityindustry.UnityIndustry;
import com.unityindustry.gameplay.UnityProgress;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Additive prototype registry: 7 new normal/elite machines + 3 bosses.
 * The existing Scrap Crawler is registered ONLY by ModEntities; do not duplicate its ID.
 * The names, size, model, combat numbers, and progression thresholds are NOT final.
 */
public final class ModMachines {
    private ModMachines() {}

    public static final List<MachineSpec> SPECS = List.of(
        new MachineSpec("rivet_hound",        1, 18,  4, 0.34, 2, 0.90f, 1.10f, 19, false),
        new MachineSpec("bolt_sentry",        2, 23,  4, 0.22, 4, 0.95f, 1.55f, 13, false),
        new MachineSpec("smelter_brute",      3, 55,  9, 0.18, 9, 1.30f, 2.65f,  9, false),
        new MachineSpec("conveyor_stalker",   3, 32,  7, 0.31, 4, 0.95f, 1.85f, 12, false),
        new MachineSpec("arc_warden",         4, 46,  8, 0.25, 7, 1.10f, 2.15f,  7, false),
        new MachineSpec("press_titan",        4, 90, 13, 0.14,12, 1.65f, 3.10f,  3, false),
        new MachineSpec("prime_enforcer",     5, 82, 12, 0.29,11, 1.15f, 2.40f,  4, false),
        new MachineSpec("furnace_tyrant",     3,250, 19, 0.18,16, 2.40f, 4.10f,  0, true),
        new MachineSpec("assembly_overlord",  4,350, 23, 0.24,18, 2.40f, 4.20f,  0, true),
        new MachineSpec("prime_engine",       5,500, 28, 0.22,24, 2.80f, 5.10f,  0, true)
    );

    public static final Map<String, EntityType<MachineEntity>> TYPES = new LinkedHashMap<>();

    public static void register() {
        for (MachineSpec spec : SPECS) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, UnityIndustry.id(spec.id()));
            // Fabric's builder registers the natural-spawn restriction before Minecraft freezes its registries.
            EntityType.Builder<MachineEntity> builder = FabricEntityType.Builder.createMob(
                MachineEntity::new, MobCategory.MONSTER,
                custom -> custom.spawnRestriction(
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) ->
                        Monster.checkMonsterSpawnRules(type, world, reason, pos, random)
                        && (1 + UnityProgress.worldStage(world.getLevel().getServer())) >= spec.minStage()
                )
            );
            EntityType<MachineEntity> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                builder.sized(spec.width(), spec.height()).build(key));
            TYPES.put(spec.id(), type);
            FabricDefaultAttributeRegistry.register(type, Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, spec.health())
                .add(Attributes.ATTACK_DAMAGE, spec.attack())
                .add(Attributes.MOVEMENT_SPEED, spec.speed())
                .add(Attributes.ARMOR, spec.armor())
                .add(Attributes.FOLLOW_RANGE, 28.0)
            );
            // Only normal and elite machines enter the natural hostile spawn pool.
            // Bosses are deliberately manual /summon targets until their summoning rules are approved.
            if (!spec.boss()) {
                BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                    MobCategory.MONSTER, type, spec.spawnWeight(), 1, 2);
            }
        }
        UnityIndustry.LOGGER.info("Registered {} prototype industrial machines", TYPES.size());
    }
}
