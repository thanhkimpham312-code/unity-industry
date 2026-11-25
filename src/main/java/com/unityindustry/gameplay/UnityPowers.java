package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import com.unityindustry.rules.SkillCatalog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import com.unityindustry.registry.ModItems;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

/**
 * Server-side powers completed in this milestone:
 * - Fire Core: free extinguish; when in lava, 2 UE/s for temporary resistance.
 * - Pressure Core: water breathing and dolphins grace while underwater (2 UE/s).
 * - Magnet Core: nearby loose item attraction (2 UE/s).
 * - Maintenance Core: repair held damaged item, 1 UE per durability point.
 * - Flying Core: manual dynamic skill, max 60 sec per activation, no cooldown, 2 UE/s.
 * - Ultra: always-on Wither immunity, Wither on melee, flight without time cap.
 * Other abilities deliberately remain nonfunctional until implemented accurately.
 */
public final class UnityPowers {
    private static final Map<UUID, Integer> FLIGHT_TICKS = new HashMap<>();
    private record HeldSkill(int slot, SkillCatalog.Entry entry) { }
    private static final Map<UUID, HeldSkill> HELD_SKILL = new HashMap<>();
    // One target for the whole hold session. Do not hop to a new target after a kill.
    private static final Map<UUID, UUID> WHITE_LASER_LOCK = new HashMap<>();
    private static final java.util.Set<UUID> WHITE_LASER_ENDED = new java.util.HashSet<>();
    private static long tick = 0L;

    private static List<CoreType> worn(ServerPlayer p) {
        ItemStack chest = p.getItemBySlot(EquipmentSlot.CHEST);
        return ChestplateCores.get(chest);
    }

    private static boolean spendEveryHalfSecond(ServerPlayer p) {
        // One UE each 10 ticks = 2 UE per second. Charged only while active.
        return UnityEnergy.get(p) > 0 && (tick % 10 != 0 || UnityEnergy.consume(p, 1L));
    }

    public static void initialize() {
        UnityExcavation.initialize();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) updatePlayer(p);
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer sp
                    && entity instanceof LivingEntity target
                    && worn(sp).contains(CoreType.ULTRA)) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            }
            return InteractionResult.PASS;
        });
    }

    /** Re-map numbers in Core installation order. The server uses its own authoritative
     *  chestplate data, so stale or spoofed client packets cannot activate an uninstalled Core.
     */
    public static void handleSkillKey(ServerPlayer player, int slot) {
        SkillCatalog.Entry skill = SkillCatalog.at(worn(player), slot);
        if (skill == null) return; // removing Core makes the number disappear
        switch (skill.action()) {
            case FLIGHT_TOGGLE -> toggleFlight(player);
            case WITHER_ROSES -> UnityAbilitiesPrototype.witherRoses(player);
            case WITHER_SKULL -> UnityAbilitiesPrototype.witherSkull(player);
            case SCAN_ORES -> UnityAbilitiesPrototype.scanOres(player);
            case GROW_CROPS -> UnityHarvest.growNearby(player);
            case OPEN_STORAGE -> UnityStorage.open(player);
            default -> { /* held actions are validated by setHeldSkill */ }
        }
    }

    public static void setHeldSkill(ServerPlayer player, int slot, boolean down) {
        UUID id = player.getUUID();
        SkillCatalog.Entry skill = SkillCatalog.at(worn(player), slot);
        if (!down) {
            HELD_SKILL.remove(id);
            WHITE_LASER_LOCK.remove(id);
            WHITE_LASER_ENDED.remove(id);
            return;
        }
        if (skill != null && skill.action().hold) {
            HeldSkill before = HELD_SKILL.put(id, new HeldSkill(slot, skill));
            if (before == null || !before.entry().equals(skill)) {
                WHITE_LASER_LOCK.remove(id);
                WHITE_LASER_ENDED.remove(id);
            }

        }
    }

    /** White Laser - aim at a Mob with the crosshair first, then lock that exact Mob.
     *  ALL Mob subclasses are valid (hostile / neutral / passive / modded).
     *  No players. The first Mob intersected by the 15-block ray wins; a wall
     *  cuts the ray off. Damage: 12 HP / 10 ticks (24 HP/s), UE: 1 / 10 ticks.
     *  No cooldown and no target switching until the key is released.
     */
    private static void whiteLaser(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        UUID caster = player.getUUID();
        if (WHITE_LASER_ENDED.contains(caster)) return;

        Mob target = null;
        UUID locked = WHITE_LASER_LOCK.get(caster);
        if (locked != null) {
            Entity entity = level.getEntity(locked);
            if (entity instanceof Mob mob) target = mob;
            if (!laserValid(player, target)) {
                WHITE_LASER_LOCK.remove(caster);
                WHITE_LASER_ENDED.add(caster);
                return;
            }
        } else {
            // Aim controls acquisition; NEVER choose the nearest mob automatically.
            target = aimedMob(level, player);
            if (target == null) return;
            WHITE_LASER_LOCK.put(caster, target.getUUID());
        }

        if (UnityEnergy.get(player) <= 0) {
            WHITE_LASER_LOCK.remove(caster);
            WHITE_LASER_ENDED.add(caster);
            return;
        }
        // One straight path from the caster's eye to this single locked Mob.
        // Particle sampling is still a provisional visual, not a solid beam mesh.
        if (tick % 2 == 0) drawLaserRay(level, player.getEyePosition(),
                target.getBoundingBox().getCenter());
        if (tick % 10 == 0) {
            if (!UnityEnergy.consume(player, 1L)) {
                WHITE_LASER_LOCK.remove(caster);
                WHITE_LASER_ENDED.add(caster);
                return;
            }
            target.hurtServer(level, level.damageSources().magic(), 12.0F);
            if (!target.isAlive()) {
                UnityProgress.award(player, "laser_kill");
                WHITE_LASER_LOCK.remove(caster);
                WHITE_LASER_ENDED.add(caster);
            }
        }
    }

    /** Select first Mob crossed by the player's 15-block look ray, before any
     * blocking terrain. Bounding-box clipping makes aim precise and independent
     * of a Mob's hostility, size, or distance to the player.
     */
    private static Mob aimedMob(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().normalize().scale(15.0));
        HitResult wall = level.clip(new ClipContext(eye, end,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxSq = wall.getType() == HitResult.Type.MISS
            ? 225.0 : eye.distanceToSqr(wall.getLocation());
        Mob nearest = null;
        double nearestSq = maxSq + 1.0e-6;
        AABB rayBounds = new AABB(eye, end).inflate(1.0);
        for (Mob candidate : level.getEntitiesOfClass(Mob.class, rayBounds,
                mob -> mob.isAlive() && mob.isPickable())) {
            var intersection = candidate.getBoundingBox().inflate(0.12).clip(eye, end);
            if (intersection.isEmpty()) continue;
            double hitSq = eye.distanceToSqr(intersection.get());
            if (hitSq <= maxSq + 1.0e-6 && hitSq < nearestSq) {
                nearest = candidate;
                nearestSq = hitSq;
            }
        }
        return nearest;
    }

    private static boolean laserValid(ServerPlayer player, Mob mob) {
        return mob != null && mob.isAlive()
            && mob.distanceToSqr(player) <= 225.0
            && player.hasLineOfSight(mob);
    }

    private static void drawLaserRay(ServerLevel level, Vec3 origin, Vec3 destination) {
        Vec3 ray = destination.subtract(origin);
        double length = ray.length();
        // One straight segment, sampled tightly so it reads as one continuous white ray.
        // Never branch or add a secondary target.
        int points = Math.max(1, Math.min(200, (int)Math.ceil(length / 0.14)));
        for (int i=0; i<=points; i++) {
            Vec3 at = origin.add(ray.scale(i / (double)points));
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z,
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static void toggleFlight(ServerPlayer p) {
        List<CoreType> equipped = worn(p);
        boolean flyingCore = equipped.contains(CoreType.FLYING);
        boolean ultra = equipped.contains(CoreType.ULTRA);
        if (!flyingCore && !ultra) return;
        if (FLIGHT_TICKS.containsKey(p.getUUID())) {
            disableFlight(p, true);
            return;
        }
        if (UnityEnergy.get(p) <= 0) return;
        FLIGHT_TICKS.put(p.getUUID(), 0);
        p.getAbilities().mayfly = true;
        p.getAbilities().flying = true;
        p.onUpdateAbilities();
    }

    private static void disableFlight(ServerPlayer p, boolean landSafely) {
        if (!p.isCreative() && !p.isSpectator()) {
            p.getAbilities().flying = false;
            p.getAbilities().mayfly = false;
            p.onUpdateAbilities();
        }
        if (landSafely) p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        FLIGHT_TICKS.remove(p.getUUID());
    }

    private static void updatePlayer(ServerPlayer p) {
        if (!UnityEnergy.unlocked(p)) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                if (p.getInventory().getItem(i).is(com.unityindustry.registry.ModItems.UNITY_INGOT)) {
                    UnityEnergy.unlock(p);
                    break;
                }
            }
        }
        // Repair legacy 0.4.3 chestplates whose EQUIPPABLE asset points to a
        // missing generated variant. This keeps installed Cores and durability.
        ItemStack wornArmor = p.getItemBySlot(EquipmentSlot.CHEST);
        if (ChestplateCores.validArmor(wornArmor)) {
            var standardEquipment = new ItemStack(ModItems.UNITY_CHESTPLATE)
                .get(DataComponents.EQUIPPABLE);
            if (standardEquipment != null
                    && !standardEquipment.equals(wornArmor.get(DataComponents.EQUIPPABLE))) {
                wornArmor.set(DataComponents.EQUIPPABLE, standardEquipment);
            }
        }
        UnityExcavation.trackMovement(p);
        List<CoreType> cores = worn(p);
        boolean ultra = cores.contains(CoreType.ULTRA);
        UUID id = p.getUUID();
        HeldSkill held = HELD_SKILL.get(id);
        if (held != null) {
            SkillCatalog.Entry stillInstalled = SkillCatalog.at(cores, held.slot());
            // Hold must never silently turn into a different skill after removing a Core.
            if (!held.entry().equals(stillInstalled) || !held.entry().action().hold) {
                HELD_SKILL.remove(id);
                WHITE_LASER_LOCK.remove(id);
                WHITE_LASER_ENDED.remove(id);
            } else if (held.entry().action() == SkillCatalog.Action.WHITE_LASER) {
                whiteLaser(p);
            } else if (held.entry().action() == SkillCatalog.Action.EXCAVATE && tick % 4 == 0) {
                UnityExcavation.tunnel(p);
            }
        }
        if (cores.contains(CoreType.FIRE)) {
            p.clearFire();
            if (p.isInLava() && spendEveryHalfSecond(p)) {
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 24, 0, true, false));
            }
        }
        if (cores.contains(CoreType.PRESSURE) && p.isUnderWater() && spendEveryHalfSecond(p)) {
            p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 35, 0, true, false));
            p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 35, 0, true, false));
        }
        if (cores.contains(CoreType.MAGNET) && tick % 5 == 0 && spendEveryHalfSecond(p)) {
            for (ItemEntity drop : p.level().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(6.0))) {
                Vec3 direction = p.position().add(0, 0.8, 0).subtract(drop.position());
                if (direction.lengthSqr() > 0.01 && direction.lengthSqr() <= 36.0)
                    drop.setDeltaMovement(drop.getDeltaMovement().add(direction.normalize().scale(0.19)));
            }
        }
        if (cores.contains(CoreType.MAINTENANCE) && tick % 5 == 0) {
            repairOneDurability(p); // one UE = one durability, prioritise equipped items
        }
        if (ultra) p.removeEffect(MobEffects.WITHER);

        if (FLIGHT_TICKS.containsKey(id)) {
            if (!(ultra || cores.contains(CoreType.FLYING)) || UnityEnergy.get(p) <= 0) {
                disableFlight(p, !p.onGround());
            } else {
                int elapsed = FLIGHT_TICKS.get(id) + 1;
                // Landing ends an active flight automatically. A short takeoff grace
                // period permits toggling flight while the player's feet touch ground.
                // This is NOT a cooldown: the player may toggle flight again at once.
                if (elapsed > 15 && p.onGround()) {
                    disableFlight(p, false);
                } else if (!ultra && elapsed >= 1200) {
                    disableFlight(p, !p.onGround());
                } else if (!p.onGround() && elapsed % 10 == 0
                        && !UnityEnergy.consume(p, 1L)) {
                    disableFlight(p, true);
                } else {
                    // No UE is consumed while grounded or merely in takeoff grace.
                    FLIGHT_TICKS.put(id, elapsed);
                }
            }
        }

        // Minimal action-bar readout until the bespoke orange XP replacement is ready.
        if (UnityEnergy.unlocked(p) && tick % 40 == 0) {
            p.displayClientMessage(Component.literal("§6Unity Energy: " + UnityEnergy.get(p) + " UE"), true);
        }
    }
    private static void repairOneDurability(ServerPlayer p) {
        if (UnityEnergy.get(p) < 1L) return;
        java.util.List<ItemStack> priority = new java.util.ArrayList<>();
        priority.add(p.getMainHandItem());
        priority.add(p.getItemBySlot(EquipmentSlot.CHEST));
        priority.add(p.getItemBySlot(EquipmentSlot.HEAD));
        priority.add(p.getItemBySlot(EquipmentSlot.LEGS));
        priority.add(p.getItemBySlot(EquipmentSlot.FEET));
        priority.add(p.getOffhandItem());
        for (int i=0;i<9;i++) priority.add(p.getInventory().getItem(i));
        for (int i=9;i<36;i++) priority.add(p.getInventory().getItem(i));
        for (ItemStack item : priority) {
            if (item.isDamageableItem() && item.getDamageValue() > 0) {
                if (UnityEnergy.consume(p, 1L)) {
                    item.setDamageValue(item.getDamageValue() - 1);
                    p.getInventory().setChanged();
                }
                break; // repair only one point per 5 ticks to avoid UE spikes
            }
        }
    }
    private UnityPowers() { }
}
