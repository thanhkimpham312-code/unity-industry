package com.unityindustry.registry;

import com.unityindustry.UnityIndustry;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.monster.zombie.Zombie;

/** Test enemy. It intentionally uses Zombie AI & renderer as a temporary prototype.
 *  This is not the final Scrap Crawler model nor the completed progressive mob roster.
 */
public final class ModEntities {
    private static final ResourceKey<EntityType<?>> SCRAP_KEY = ResourceKey.create(
        Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "scrap_crawler"));
    public static final EntityType<Zombie> SCRAP_CRAWLER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, SCRAP_KEY,
        FabricEntityType.Builder.<Zombie>createMob(Zombie::new, MobCategory.MONSTER, mob -> {
            mob.defaultAttributes(Zombie::createAttributes);
            mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
            return mob;
        }).sized(0.6f, 1.95f).clientTrackingRange(8).build(SCRAP_KEY));
    public static void initialize() {
        // Low spawn weight for the prototype. Progression tiers not yet defined.
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
            MobCategory.MONSTER, SCRAP_CRAWLER, 10, 1, 1);
    }
    private ModEntities() { }
}
