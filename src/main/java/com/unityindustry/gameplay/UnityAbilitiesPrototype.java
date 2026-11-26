package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.unityindustry.network.OreScanPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Experimental actions beyond the original 0.4.6 prototype.
 * Scanner sends bounded ore positions to client for bright block-shaped contours.
 * Rose cloud and skull blast use visible particles, not bespoke models/physics.
 * A safe server-side baseline while asset / render features are developed.
 */
public final class UnityAbilitiesPrototype {
    private record Scan(long until, long nextCharge) { }
    private record RoseField(ServerLevel level, UUID owner, Vec3 at, long until) { }
    private static final TagKey<Block> COMMON_ORES = TagKey.create(Registries.BLOCK,
        Identifier.fromNamespaceAndPath("c", "ores"));
    private static final Map<UUID, Scan> SCANS = new HashMap<>();
    private static final List<RoseField> ROSES = new ArrayList<>();
    // Wide area: 10-block horizontal radius, 3 blocks below and 6 above.
    // Damage is kept at 4 HP per 10 ticks (8 HP/s) + Wither II.
    private static final double ROSE_RADIUS = 10.0D;
    private static final double ROSE_RADIUS_SQ = ROSE_RADIUS * ROSE_RADIUS;

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                Scan scan = SCANS.get(p.getUUID());
                if (scan == null) continue;
                long now = p.level().getGameTime();
                if (now >= scan.until() ||
                        !ChestplateCores.get(p.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ORE_SCANNER)) {
                    SCANS.remove(p.getUUID());
                    ServerPlayNetworking.send(p, new OreScanPayload(""));
                    continue;
                }
                if (now >= scan.nextCharge()) {
                    if (UnityEnergy.consume(p, 1L)) {
                        SCANS.put(p.getUUID(), new Scan(scan.until(), now + 10));
                    } else {
                        SCANS.remove(p.getUUID());
                        ServerPlayNetworking.send(p, new OreScanPayload(""));
                    }
                }
            }
            var it = ROSES.iterator();
            while (it.hasNext()) {
                RoseField r = it.next();
                long now = r.level().getGameTime();
                if (now > r.until()) { it.remove(); continue; }
                if (now % 10 != 0) continue;
                // Large cylindrical field: all Mob subclasses (passive, neutral,
                // hostile and modded). Players are not Mob instances.
                // Use a capped number of animated particles to limit network load.
                for (int i = 0; i < 36; i++) {
                    double angle = i * (Math.PI * 2.0D / 36.0D) + now * 0.025D;
                    double ring = switch (i % 3) {
                        case 0 -> 2.5D;
                        case 1 -> 6.0D;
                        default -> ROSE_RADIUS - 0.35D;
                    };
                    Vec3 at = r.at().add(Math.cos(angle) * ring, 0, Math.sin(angle) * ring);
                    r.level().sendParticles(ParticleTypes.WITCH, at.x, at.y + 0.35D, at.z,
                        1, 0.10D, 0.25D, 0.10D, 0.01D);
                }
                AABB effectBounds = new AABB(
                    r.at().add(-ROSE_RADIUS, -3.0D, -ROSE_RADIUS),
                    r.at().add(ROSE_RADIUS, 6.0D, ROSE_RADIUS));
                for (Mob mob : r.level().getEntitiesOfClass(Mob.class, effectBounds,
                        m -> m.isAlive()
                            && (m.getX() - r.at().x) * (m.getX() - r.at().x)
                             + (m.getZ() - r.at().z) * (m.getZ() - r.at().z) <= ROSE_RADIUS_SQ)) {
                    // 4 HP per 10 ticks = 8 HP/s on all mobs, plus Wither II.
                    // Undead may be immune to the Wither effect, but still receive direct damage.
                    mob.hurtServer(r.level(), r.level().damageSources().magic(), 4.0F);
                    if (mob.isAlive()) mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 1));
                }
            }
        });
    }

    /** Scan and reveal the closest 96 ore blocks in a 16-block sphere.
     *  Never edits ore; scan continues for 200 ticks, charges 1 UE / 10 ticks,
     *  and may be restarted immediately (NO COOLDOWN).
     */
    public static void scanOres(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ORE_SCANNER)) return;
        if (UnityEnergy.get(player) < 1L) return;
        long now = level.getGameTime();
        BlockPos center = player.blockPosition();
        List<BlockPos> ores = new ArrayList<>();
        // Bounded 3D radius, runs only on a manual scan (not on each tick).
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16,-16,-16),center.offset(16,16,16))) {
            if (pos.distSqr(center) > 256 || !level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if (state.is(COMMON_ORES) || name.endsWith("_ore") || name.equals("ancient_debris"))
                ores.add(pos.immutable());
        }
        ores.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
        int count = Math.min(96,ores.size()); // protect network/per-frame renderer
        StringBuilder packed = new StringBuilder();
        for (int i=0; i<count; i++) {
            if (i>0) packed.append(';');
            packed.append(ores.get(i).asLong());
        }
        ServerPlayNetworking.send(player, new OreScanPayload(packed.toString()));
        player.displayClientMessage(Component.literal("§bOre Scanner: " + ores.size()
            + " quặng trong bán kính 16; hiện " + count + " viền gần nhất trong 10 giây"), true);
        SCANS.put(player.getUUID(),new Scan(now+200,now));
    }

    public static void witherRoses(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ULTRA)) return;
        if (!UnityEnergy.consume(player, 10L)) return;
        // Only one active field per player: stronger damage must not stack without a bound.
        ROSES.removeIf(field -> field.owner().equals(player.getUUID()));
        ROSES.add(new RoseField(level, player.getUUID(), player.position(), level.getGameTime() + 200));
    }

    /** Ultra Core skill 3: high-power terrain-breaking skull explosion.
     *  Blocks along the look ray are hit first; no detonating through walls.
     *  The vanilla TNT explosion interaction handles terrain and entity damage.
     */
    public static void witherSkull(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ULTRA)) return;
        if (UnityEnergy.get(player) < 5L) return;

        Vec3 source = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 far = source.add(look.scale(20.0));
        HitResult wall = level.clip(new ClipContext(source, far,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double wallRange = wall.getType() == HitResult.Type.MISS
            ? 20.0 : Math.min(20.0, source.distanceTo(wall.getLocation()));

        Mob target = null;
        double range = wallRange;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(20.0),
                m -> m.isAlive() && m.getType().getCategory() == MobCategory.MONSTER
                    && player.hasLineOfSight(m))) {
            Vec3 to = mob.getBoundingBox().getCenter().subtract(source);
            double along = to.dot(look);
            if (along < 0 || along > range
                    || to.subtract(look.scale(along)).lengthSqr() > 1.8) continue;
            range = along;
            target = mob;
        }
        if (!UnityEnergy.consume(player, 5L)) return;

        Vec3 endpoint = target == null
            ? source.add(look.scale(wallRange))
            : target.getBoundingBox().getCenter();
        Vec3 direction = endpoint.subtract(source);
        for (int i = 0; i <= 40; i++) {
            Vec3 at = source.add(direction.scale(i / 40.0));
            level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z,
                1, 0, 0, 0, 0.0);
        }
        // Explosion power 5 (TNT = 4). Vanilla handles knockback, damage,
        // block resistance, drops and world/server explosion rules.
        // CASTER CAN BE HURT: back up before firing near your own structures.
        level.explode(player, endpoint.x, endpoint.y, endpoint.z, 5.0F,
            Level.ExplosionInteraction.TNT);
    }
    private UnityAbilitiesPrototype() { }
}
