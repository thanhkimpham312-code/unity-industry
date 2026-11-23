package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.ScrapCrawlerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.levelgen.Heightmap;

/** Scrap Crawler: distinct 8-legged hostile prototype, not a Zombie.
 *  The custom industrial texture is registered in the client initializer.
 */
public final class ModEntities {
    private static final ResourceKey<EntityType<?>> SCRAP_KEY = ResourceKey.create(
        Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "scrap_crawler"));

    public static final EntityType<ScrapCrawlerEntity> SCRAP_CRAWLER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, SCRAP_KEY,
        FabricEntityType.Builder.<ScrapCrawlerEntity>createMob(
            ScrapCrawlerEntity::new, MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(Spider::createAttributes);
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules);
                return mob;
            })
            .sized(1.2f, 0.8f)
            .clientTrackingRange(8)
            .build(SCRAP_KEY));

    public static void initialize() {
        // Natural spawn remains a prototype. Progression scaling not implemented.
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, SCRAP_CRAWLER, 10, 1, 1);
    }
    private ModEntities() { }
}
