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
                // All Mob subclasses (passive, neutral, hostile and modded) are
                // affected; players are excluded. The field visuals are bounded
                // at 160 particles per 10 ticks to avoid runaway particle spam.
                roseVisuals(r.level(), r.at(), now);
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
        // Only one active field per player: strong damage must not stack.
        ROSES.removeIf(field -> field.owner().equals(player.getUUID()));
        Vec3 origin = player.position();
        ROSES.add(new RoseField(level, player.getUUID(), origin, level.getGameTime() + 200));
        // Initial bloom: a dramatic pulse of purple rose mist and soul sparks.
        level.sendParticles(ParticleTypes.WITCH, origin.x, origin.y + 0.7D, origin.z,
            64, 2.5D, 0.9D, 2.5D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, origin.x, origin.y + 0.7D, origin.z,
            48, 1.8D, 1.3D, 1.8D, 0.12D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, origin.x, origin.y + 0.3D, origin.z,
            24, 1.1D, 0.3D, 1.1D, 0.01D);
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
        // Arcane twin-helix trail + a hollow skull-like energy core.
        // This is cosmetic and does not delay the already validated explosion.
        skullFlightVisuals(level, source, endpoint);
        skullImpactVisuals(level, endpoint);
        // Explosion power 5 (TNT = 4). Vanilla handles knockback, damage,
        // block resistance, drops and world/server explosion rules.
        // CASTER CAN BE HURT: back up before firing near your own structures.
        level.explode(player, endpoint.x, endpoint.y, endpoint.z, 5.0F,
            Level.ExplosionInteraction.TNT);
    }

    /**
     * Four concentric counter-rotating rose-petal rings and a rising soul vortex.
     * Called once every 10 ticks for each active field. 48 ring clusters x 3
     * plus 8 vortex clusters x 2 = 160 particles / half-second / field.
     */
    private static void roseVisuals(ServerLevel level, Vec3 center, long gameTime) {
        final int sectors = 12;
        for (int ring = 0; ring < 4; ring++) {
            double radius = switch (ring) {
                case 0 -> 2.5D;
                case 1 -> 5.0D;
                case 2 -> 7.5D;
                default -> 9.8D;
            };
            double angularSpeed = (ring % 2 == 0 ? 0.038D : -0.030D);
            for (int i = 0; i < sectors; i++) {
                double angle = (2.0D * Math.PI * i / sectors)
                    + gameTime * angularSpeed + ring * 0.45D;
                double height = 0.40D + ring * 0.56D
                    + Math.sin(gameTime * 0.065D + i * 0.7D) * 0.38D;
                double px = center.x + Math.cos(angle) * radius;
                double pz = center.z + Math.sin(angle) * radius;
                // Purple rose petals, dark portal haze, and soul-blue rim.
                var particle = switch (ring) {
                    case 0, 2 -> ParticleTypes.WITCH;
                    case 1 -> ParticleTypes.REVERSE_PORTAL;
                    default -> ParticleTypes.SOUL_FIRE_FLAME;
                };
                level.sendParticles(particle, px, center.y + height, pz,
                    3, 0.24D, 0.30D, 0.24D, 0.012D);
            }
        }
        // A short twisting column of souls rises in the middle.
        for (int i = 0; i < 8; i++) {
            double angle = gameTime * 0.08D + i * Math.PI / 4.0D;
            double radius = 0.6D + (i % 3) * 0.46D;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                center.x + Math.cos(angle) * radius,
                center.y + 0.65D + i * 0.46D,
                center.z + Math.sin(angle) * radius,
                2, 0.10D, 0.22D, 0.10D, 0.015D);
        }
    }

    /**
     * A denser dark-purple projectile path: luminous central smoke, a pair
     * of counterbalancing soul trails and a flickering wither core.
     * An instantaneous particle visualization: not a new persistent entity.
     */
    private static void skullFlightVisuals(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 travel = to.subtract(from);
        double distance = travel.length();
        if (distance < 0.01D) return;

        Vec3 forward = travel.normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 0.0001D) right = new Vec3(1.0D, 0.0D, 0.0D);
        right = right.normalize();
        Vec3 up = forward.cross(right).normalize();
        int steps = Math.max(10, Math.min(54, (int) Math.ceil(distance / 0.36D)));

        // Two alternating arcs twist around the projectile's central spine.
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 axis = from.add(travel.scale(t));
            double turn = t * Math.PI * 10.0D;
            double width = 0.14D + 0.32D * Math.sin(Math.PI * t);
            Vec3 offset = right.scale(Math.cos(turn) * width)
                .add(up.scale(Math.sin(turn) * width));
            Vec3 a = axis.add(offset);
            Vec3 b = axis.subtract(offset);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                a.x, a.y, a.z, 2, 0.045D, 0.045D, 0.045D, 0.003D);
            level.sendParticles(ParticleTypes.WITCH,
                b.x, b.y, b.z, 2, 0.055D, 0.055D, 0.055D, 0.003D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    axis.x, axis.y, axis.z, 2, 0.08D, 0.08D, 0.08D, 0.012D);
            }
            if (i % 5 == 0) {
                level.sendParticles(ParticleTypes.SMOKE,
                    axis.x, axis.y, axis.z, 2, 0.13D, 0.13D, 0.13D, 0.012D);
            }
        }
    }

    /** Three-dimensional energy rings plus a dense purple-black explosion bloom. */
    private static void skullImpactVisuals(ServerLevel level, Vec3 point) {
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.4D + ring * 1.35D;
            for (int i = 0; i < 16; i++) {
                double angle = 2.0D * Math.PI * i / 16.0D + ring * 0.20D;
                double x = point.x + Math.cos(angle) * radius;
                double y = point.y + (ring == 1 ? Math.sin(angle) * radius : 0.25D);
                double z = point.z + (ring == 1 ? 0.0D : Math.sin(angle) * radius);
                var particle = ring == 0 ? ParticleTypes.SOUL_FIRE_FLAME
                    : ring == 1 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.WITCH;
                level.sendParticles(particle, x, y, z,
                    2, 0.08D, 0.08D, 0.08D, 0.03D);
            }
        }
        level.sendParticles(ParticleTypes.WITCH, point.x, point.y, point.z,
            70, 1.4D, 1.3D, 1.4D, 0.14D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z,
            55, 1.2D, 1.4D, 1.2D, 0.18D);
        level.sendParticles(ParticleTypes.SMOKE, point.x, point.y, point.z,
            65, 1.5D, 0.9D, 1.5D, 0.10D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z,
            44, 1.2D, 0.8D, 1.2D, 0.055D);
        level.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y, point.z,
            5, 0.8D, 0.8D, 0.8D, 0.02D);
    }

    private UnityAbilitiesPrototype() { }
}
