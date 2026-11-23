package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import net.minecraft.world.phys.Vec3;

/**
 * Server-side powers completed in this milestone:
 * - Fire Core: free extinguish; when in lava, 2 UE/s for temporary resistance.
 * - Pressure Core: water breathing and dolphins grace while underwater (2 UE/s).
 * - Magnet Core: nearby loose item attraction (2 UE/s).
 * - Maintenance Core: repair held damaged item, 1 UE per durability point.
 * - Flying Core: X? no, toggle G, 60 sec maximum, 30 sec cooldown, 2 UE/s.
 * - Ultra: always-on Wither immunity, Wither on melee, flight without time cap.
 * Other abilities deliberately remain nonfunctional until implemented accurately.
 */
public final class UnityPowers {
    private static final Map<UUID, Integer> FLIGHT_TICKS = new HashMap<>();
    private static final Map<UUID, Long> FLIGHT_COOLDOWN_UNTIL = new HashMap<>();
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

    /** Numeric skill hotkeys are registered and dispatched through server validation.
     * Slot 2 reuses the existing flight toggle; all other actions are pending.
     */
    public static void handleSkillKey(ServerPlayer player, int slot) {
        if (slot < 1 || slot > 9) return;
        List<CoreType> cores = worn(player);
        if (cores.isEmpty()) {
            player.displayClientMessage(Component.literal("Chưa cấy Core vào Unity Chestplate!"), true);
            return;
        }
        if (slot == 2 && (cores.contains(CoreType.FLYING) || cores.contains(CoreType.ULTRA))) {
            toggleFlight(player);
            return;
        }
        // Never pretend an unsupported ability has actually activated.
        player.displayClientMessage(Component.literal("Kỹ năng " + slot + " chưa được lập trình ở bản 0.4.2"), true);
    }

    public static void toggleFlight(ServerPlayer p) {
        List<CoreType> equipped = worn(p);
        boolean flyingCore = equipped.contains(CoreType.FLYING);
        boolean ultra = equipped.contains(CoreType.ULTRA);
        if (!flyingCore && !ultra) return;
        if (FLIGHT_TICKS.containsKey(p.getUUID())) {
            if (flyingCore && !ultra) FLIGHT_COOLDOWN_UNTIL.put(p.getUUID(), tick + 600);
            disableFlight(p, true);
            return;
        }
        if (tick < FLIGHT_COOLDOWN_UNTIL.getOrDefault(p.getUUID(), 0L)) {
            p.displayClientMessage(Component.literal("Flying Core dang hoi chieu"), true);
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
        List<CoreType> cores = worn(p);
        boolean ultra = cores.contains(CoreType.ULTRA);
        UUID id = p.getUUID();
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
                disableFlight(p, true);
            } else {
                int elapsed = FLIGHT_TICKS.get(id) + 1;
                if (!ultra && elapsed >= 1200) {
                    FLIGHT_COOLDOWN_UNTIL.put(id, tick + 600);
                    disableFlight(p, true);
                } else if (elapsed % 10 == 0 && !UnityEnergy.consume(p, 1L)) {
                    disableFlight(p, true);
                } else FLIGHT_TICKS.put(id, elapsed);
            }
        }

        // Minimal action-bar readout until the bespoke orange XP replacement is ready.
        if (UnityEnergy.unlocked(p) && tick % 40 == 0) {
            p.displayClientMessage(Component.literal("§6Unity Energy: " + UnityEnergy.get(p) + " UE"), true);
        }
    }
    private UnityPowers() { }
}
