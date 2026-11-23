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
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Experimental actions beyond the original 0.4.6 prototype.
 * Scanner currently reports real ore coordinates, not translucent x-ray boxes.
 * Rose cloud and skull blast use visible particles, not bespoke models/physics.
 * A safe server-side baseline while asset / render features are developed.
 */
public final class UnityAbilitiesPrototype {
    private record Scan(long until, long nextCharge) { }
    private record RoseField(ServerLevel level, UUID owner, Vec3 at, long until) { }
    private static final Map<UUID, Long> SCAN_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Scan> SCANS = new HashMap<>();
    private static final List<RoseField> ROSES = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                Scan scan = SCANS.get(p.getUUID());
                if (scan == null) continue;
                long now = p.level().getGameTime();
                if (now >= scan.until() ||
                        !ChestplateCores.get(p.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ORE_SCANNER)) {
                    SCANS.remove(p.getUUID()); continue;
                }
                if (now >= scan.nextCharge()) {
                    if (UnityEnergy.consume(p, 1L)) {
                        SCANS.put(p.getUUID(), new Scan(scan.until(), now + 10));
                    } else SCANS.remove(p.getUUID());
                }
            }
            var it = ROSES.iterator();
            while (it.hasNext()) {
                RoseField r = it.next();
                long now = r.level().getGameTime();
                if (now > r.until()) { it.remove(); continue; }
                if (now % 10 != 0) continue;
                for (int dx=-2;dx<=2;dx++) for(int dz=-2;dz<=2;dz++) {
                    Vec3 at = r.at().add(dx,0,dz);
                    r.level().sendParticles(ParticleTypes.WITCH, at.x, at.y+0.25,at.z,
                        1, 0,0.15,0,0.01);
                }
                for (Mob mob : r.level().getEntitiesOfClass(Mob.class,
                        new AABB(r.at().add(-2.5,-0.2,-2.5),r.at().add(2.5,2.5,2.5)),
                        m -> m.isAlive() && m.getType().getCategory() == MobCategory.MONSTER)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 0));
                }
            }
        });
    }

    public static void scanOres(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ORE_SCANNER)) return;
        long now = level.getGameTime();
        if (now < SCAN_COOLDOWN.getOrDefault(player.getUUID(), 0L)) {
            player.displayClientMessage(Component.literal("Ore Scanner đang hồi chiêu"), true);
            return;
        }
        if (UnityEnergy.get(player) < 1L) return;
        BlockPos center = player.blockPosition();
        List<BlockPos> ores = new ArrayList<>();
        // True 3D radius, bounded at 16 blocks. Uses block ids, no world edits.
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16,-16,-16),center.offset(16,16,16))) {
            if (pos.distSqr(center) > 256) continue;
            var block = level.getBlockState(pos).getBlock();
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            if (name.endsWith("_ore") || name.startsWith("deepslate_") && name.endsWith("_ore"))
                ores.add(pos.immutable());
        }
        ores.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
        StringBuilder msg = new StringBuilder("§bOre Scanner (16b): " + ores.size() + " quặng");
        for (int i=0;i<Math.min(6,ores.size());i++) {
            BlockPos pos=ores.get(i);
            String type=BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
            msg.append("\n§e").append(type).append(": ")
                .append(pos.getX()).append(",").append(pos.getY()).append(",").append(pos.getZ());
        }
        player.displayClientMessage(Component.literal(msg.toString()), false);
        SCANS.put(player.getUUID(),new Scan(now+200,now));
        SCAN_COOLDOWN.put(player.getUUID(),now+600); // 30s; continuous UE while active
    }

    public static void witherRoses(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ULTRA)) return;
        if (!UnityEnergy.consume(player, 10L)) return;
        // Non-harvestable visual field. Real Wither Rose block models not yet generated.
        ROSES.add(new RoseField(level, player.getUUID(),player.position(),level.getGameTime()+200));
    }

    public static void witherSkull(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.ULTRA)) return;
        if (UnityEnergy.get(player) < 5L) return;
        Vec3 source = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Mob target = null;
        double range = 20.0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(range),
                m -> m.isAlive() && m.getType().getCategory()==MobCategory.MONSTER && player.hasLineOfSight(m))) {
            Vec3 to = mob.getBoundingBox().getCenter().subtract(source);
            double along = to.dot(look);
            if (along<0 || along>range || to.subtract(look.scale(along)).lengthSqr()>1.8) continue;
            if (along<range) { range=along; target=mob; }
        }
        if (!UnityEnergy.consume(player, 5L)) return;
        Vec3 endpoint=target==null?source.add(look.scale(20)):target.getBoundingBox().getCenter();
        Vec3 d=endpoint.subtract(source);
        for(int i=0;i<=40;i++){
            Vec3 at=source.add(d.scale(i/40.0));
            level.sendParticles(ParticleTypes.SMOKE,at.x,at.y,at.z,1,0,0,0,0.0);
        }
        level.sendParticles(ParticleTypes.EXPLOSION,endpoint.x,endpoint.y,endpoint.z,1,0,0,0,0);
        for (Mob mob : level.getEntitiesOfClass(Mob.class,new AABB(endpoint,endpoint).inflate(2.0),
                m -> m.isAlive() && m.getType().getCategory()==MobCategory.MONSTER)) {
            mob.hurtServer(level,level.damageSources().magic(),20.0F);
        }
        // Intentionally no terrain explosion. This is a hitscan prototype, NOT a flying skull model.
    }
    private UnityAbilitiesPrototype() { }
}
