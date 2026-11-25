package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.ScrapCrawlerEntity;
import com.unityindustry.entity.MiningDroneEntity;
import com.unityindustry.entity.CrystalBeetleEntity;
import com.unityindustry.entity.SecuritySentinelEntity;
import com.unityindustry.entity.ExcavatorMk1Entity;
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
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.levelgen.Heightmap;

/** Same five entity IDs, revised navigation and default attributes for V2. */
public final class ModEntities {
    private static ResourceKey<EntityType<?>> key(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, id));
    }
    private static final ResourceKey<EntityType<?>> SCRAP_KEY = key("scrap_crawler");
    private static final ResourceKey<EntityType<?>> DRONE_KEY = key("mining_drone");
    private static final ResourceKey<EntityType<?>> BEETLE_KEY = key("crystal_beetle");
    private static final ResourceKey<EntityType<?>> SENTINEL_KEY = key("security_sentinel");
    private static final ResourceKey<EntityType<?>> EXCAVATOR_KEY = key("excavator_mk1");

    public static final EntityType<ScrapCrawlerEntity> SCRAP_CRAWLER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, SCRAP_KEY,
        FabricEntityType.Builder.<ScrapCrawlerEntity>createMob(ScrapCrawlerEntity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> Spider.createAttributes()
                    .add(Attributes.ATTACK_DAMAGE, 3D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
                return mob;
            }).sized(1.2F, 0.8F).clientTrackingRange(8).build(SCRAP_KEY));

    public static final EntityType<MiningDroneEntity> MINING_DRONE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, DRONE_KEY,
        FabricEntityType.Builder.<MiningDroneEntity>createMob(MiningDroneEntity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> Blaze.createAttributes()
                    .add(Attributes.MAX_HEALTH, 16D).add(Attributes.ATTACK_DAMAGE, 3D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
                return mob;
            }).sized(0.8F, 1.3F).clientTrackingRange(8).build(DRONE_KEY));

    public static final EntityType<CrystalBeetleEntity> CRYSTAL_BEETLE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, BEETLE_KEY,
        FabricEntityType.Builder.<CrystalBeetleEntity>createMob(CrystalBeetleEntity::new,
            MobCategory.MONSTER, mob -> {
                // Spider superclass intentionally removes old CaveSpider poison ability.
                mob.defaultAttributes(() -> Spider.createAttributes()
                    .add(Attributes.MAX_HEALTH, 20D).add(Attributes.ATTACK_DAMAGE, 3D)
                    .add(Attributes.ARMOR, 7D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
                return mob;
            }).sized(1.12F, 0.8F).clientTrackingRange(8).build(BEETLE_KEY));

    public static final EntityType<SecuritySentinelEntity> SECURITY_SENTINEL = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, SENTINEL_KEY,
        FabricEntityType.Builder.<SecuritySentinelEntity>createMob(SecuritySentinelEntity::new,
            MobCategory.MONSTER, mob -> {
                // Monster = ground quadruped; never a flying Blaze.
                mob.defaultAttributes(() -> Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 30D).add(Attributes.ATTACK_DAMAGE, 5D)
                    .add(Attributes.ARMOR, 8D).add(Attributes.FOLLOW_RANGE, 32D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) ->
                        UnityProgress.worldStage(world.getLevel().getServer()) >= 2 &&
                        Monster.checkMonsterSpawnRules(type, world, reason, pos, random));
                return mob;
            }).sized(2.45F, 2.15F).clientTrackingRange(10).build(SENTINEL_KEY));

    public static final EntityType<ExcavatorMk1Entity> EXCAVATOR_MK1 = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, EXCAVATOR_KEY,
        FabricEntityType.Builder.<ExcavatorMk1Entity>createMob(ExcavatorMk1Entity::new,
            MobCategory.MONSTER, mob -> {
                mob.defaultAttributes(() -> IronGolem.createAttributes()
                    .add(Attributes.MAX_HEALTH, 90D).add(Attributes.ATTACK_DAMAGE, 11D));
                mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) ->
                        UnityProgress.worldStage(world.getLevel().getServer()) >= 4 &&
                        Monster.checkMonsterSpawnRules(type, world, reason, pos, random));
                return mob;
            }).sized(3.55F, 3.15F).clientTrackingRange(12).build(EXCAVATOR_KEY));

    public static void initialize() {
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
