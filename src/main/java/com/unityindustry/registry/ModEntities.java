package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.ScrapCrawlerEntity;
import com.unityindustry.entity.MiningDroneEntity;
import com.unityindustry.entity.CrystalBeetleEntity;
import com.unityindustry.entity.SecuritySentinelEntity;
import com.unityindustry.entity.ExcavatorMk1Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.animal.golem.IronGolem;
import com.unityindustry.gameplay.UnityProgress;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
                // Vanilla Spider base melee damage is 2.0; Scrap Crawler deals 3.0 (150%).
                mob.defaultAttributes(() -> Spider.createAttributes()
                    .add(Attributes.ATTACK_DAMAGE, 3.0D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules);
                return mob;
            })
            .sized(1.2f, 0.8f)
            .clientTrackingRange(8)
            .build(SCRAP_KEY));

    private static final ResourceKey<EntityType<?>> DRONE_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "mining_drone"));
    private static final ResourceKey<EntityType<?>> BEETLE_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "crystal_beetle"));
    private static final ResourceKey<EntityType<?>> SENTINEL_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "security_sentinel"));
    private static final ResourceKey<EntityType<?>> EXCAVATOR_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "excavator_mk1"));

    // Each type has its own registry id, spawn egg, default attributes and AI class.
    // Attribute values and visuals are PROTOTYPE, not final-approved balance.
    public static final EntityType<MiningDroneEntity> MINING_DRONE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, DRONE_KEY,
        FabricEntityType.Builder.<MiningDroneEntity>createMob(MiningDroneEntity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> Blaze.createAttributes()
                    .add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.ATTACK_DAMAGE, 3.0D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
                return mob;
            }).sized(0.8f, 1.3f).clientTrackingRange(8).build(DRONE_KEY));

    public static final EntityType<CrystalBeetleEntity> CRYSTAL_BEETLE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, BEETLE_KEY,
        FabricEntityType.Builder.<CrystalBeetleEntity>createMob(CrystalBeetleEntity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> CaveSpider.createCaveSpider()
                    .add(Attributes.MAX_HEALTH, 18.0D).add(Attributes.ATTACK_DAMAGE, 3.0D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
                return mob;
            }).sized(0.9f, 0.55f).clientTrackingRange(8).build(BEETLE_KEY));

    public static final EntityType<SecuritySentinelEntity> SECURITY_SENTINEL = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, SENTINEL_KEY,
        FabricEntityType.Builder.<SecuritySentinelEntity>createMob(SecuritySentinelEntity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> Blaze.createAttributes()
                    .add(Attributes.MAX_HEALTH, 26.0D).add(Attributes.ATTACK_DAMAGE, 5.0D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) ->
                        UnityProgress.worldStage(world.getLevel().getServer()) >= 2 &&
                        Monster.checkMonsterSpawnRules(type, world, reason, pos, random));
                return mob;
            }).sized(0.85f, 1.6f).clientTrackingRange(8).build(SENTINEL_KEY));

    public static final EntityType<ExcavatorMk1Entity> EXCAVATOR_MK1 = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, EXCAVATOR_KEY,
        FabricEntityType.Builder.<ExcavatorMk1Entity>createMob(ExcavatorMk1Entity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> IronGolem.createAttributes()
                    .add(Attributes.MAX_HEALTH, 90.0D).add(Attributes.ATTACK_DAMAGE, 11.0D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) ->
                        UnityProgress.worldStage(world.getLevel().getServer()) >= 4 &&
                        Monster.checkMonsterSpawnRules(type, world, reason, pos, random));
                return mob;
            }).sized(1.5f, 2.9f).clientTrackingRange(10).build(EXCAVATOR_KEY));

    public static void initialize() {
        // Biome populations are provisional; harder types become naturally
        // eligible only after the associated world progression threshold.
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, SCRAP_CRAWLER, 10, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, MINING_DRONE, 7, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, CRYSTAL_BEETLE, 6, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, SECURITY_SENTINEL, 4, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, EXCAVATOR_MK1, 1, 1, 1);
    }
    private ModEntities() { }
}
