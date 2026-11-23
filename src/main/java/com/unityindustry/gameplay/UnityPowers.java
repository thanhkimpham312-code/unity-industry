package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import com.unityindustry.rules.SkillCatalog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
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
            case WITHER_ROSES, WITHER_SKULL, OPEN_STORAGE, SCAN_ORES ->
                player.displayClientMessage(Component.literal(
                    skill.action().label + " chưa có chức năng trong bản thử nghiệm"), true);
            default -> { /* held actions are validated by setHeldSkill */ }
        }
    }

    public static void setHeldSkill(ServerPlayer player, int slot, boolean down) {
        UUID id = player.getUUID();
        SkillCatalog.Entry skill = SkillCatalog.at(worn(player), slot);
        if (!down) { HELD_SKILL.remove(id); return; }
        if (skill != null && skill.action().hold) {
            HELD_SKILL.put(id, new HeldSkill(slot, skill));
            if (skill.action() == SkillCatalog.Action.EXCAVATE)
                player.displayClientMessage(Component.literal("Excavation 3x3: chưa có xử lý đào an toàn"), true);
        }
    }

    /** White Laser: automatic nearest visible hostile target up to 15 blocks.
     *  5 HP damage / 10 ticks = 10 HP per second. 1 UE / 10 ticks = 2 UE/s.
     *  No cooldown and no max-duration. A released key spends no UE.
     *  The server is authoritative for line of sight, damage and payment.
     */
    private static void whiteLaser(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || UnityEnergy.get(player) < 1L) return;
        var targets = level.getEntitiesOfClass(Monster.class,
            player.getBoundingBox().inflate(15.0),
            target -> target.isAlive() && target.distanceToSqr(player) <= 225.0 && player.hasLineOfSight(target));
        Monster nearest = null;
        double minDistance = 225.01;
        for (Monster monster : targets) {
            double dist = monster.distanceToSqr(player);
            if (dist < minDistance) { nearest = monster; minDistance = dist; }
        }
        if (nearest == null || !UnityEnergy.consume(player, 1L)) return;
        Vec3 start = player.getEyePosition();
        Vec3 end = nearest.getBoundingBox().getCenter();
        Vec3 diff = end.subtract(start);
        int steps = Math.max(1, (int)Math.ceil(diff.length() * 3.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(diff.scale(i / (double)steps));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                1, 0.0, 0.0, 0.0, 0.0);
        }
        nearest.hurtServer(level, level.damageSources().magic(), 5.0F);
        if (!nearest.isAlive()) UnityProgress.award(player, "laser_kill");
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
        List<CoreType> cores = worn(p);
        boolean ultra = cores.contains(CoreType.ULTRA);
        UUID id = p.getUUID();
        HeldSkill held = HELD_SKILL.get(id);
        if (held != null) {
            SkillCatalog.Entry stillInstalled = SkillCatalog.at(cores, held.slot());
            // Hold must never silently turn into a different skill after removing a Core.
            if (!held.entry().equals(stillInstalled) || !held.entry().action().hold) {
                HELD_SKILL.remove(id);
            } else if (held.entry().action() == SkillCatalog.Action.WHITE_LASER && tick % 10 == 0) {
                whiteLaser(p);
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
        if (cores.contains(CoreType.MAINTENANCE) && tick % 20 == 0) {
            ItemStack held = p.getMainHandItem();
            if (held.isDamaged() && UnityEnergy.consume(p, 1L))
                held.setDamageValue(Math.max(0, held.getDamageValue() - 1));
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
    private UnityPowers() { }
}
