package com.unityindustry.entity;

import com.unityindustry.sound.ReworkSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Shared server-authoritative action helpers. Does not modify terrain. */
public final class ReworkActions {
    private ReworkActions() { }

    public static boolean canHit(Mob mob, LivingEntity target, double range) {
        return target != null && target.isAlive() &&
            (!(target instanceof Player p) || (!p.isCreative() && !p.isSpectator())) &&
            mob.distanceToSqr(target) <= range * range && mob.getSensing().hasLineOfSight(target);
    }

    public static void cue(Mob mob, String machine, String cue, float volume) {
        mob.playSound(ReworkSounds.cue(machine, cue), volume, 0.93F + mob.getRandom().nextFloat() * 0.13F);
    }

    public static void warning(ServerLevel server, Mob mob, int count) {
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
            mob.getX(), mob.getY() + mob.getBbHeight() * 0.68D, mob.getZ(),
            count, 0.45D, 0.4D, 0.45D, 0.02D);
    }

    public static void impact(ServerLevel server, Mob mob, int count) {
        server.sendParticles(ParticleTypes.CRIT,
            mob.getX(), mob.getY() + 0.5D, mob.getZ(),
            count, 0.8D, 0.25D, 0.8D, 0.14D);
    }

    public static void beam(ServerLevel server, Mob mob, LivingEntity target) {
        double sx = mob.getX(), sy = mob.getY() + mob.getBbHeight() * 0.76D, sz = mob.getZ();
        double dx = target.getX() - sx, dy = target.getY() + target.getBbHeight() * 0.55D - sy, dz = target.getZ() - sz;
        for (int i = 1; i <= 10; i++) {
            double t = i / 11D;
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, sx + dx*t, sy + dy*t, sz + dz*t,
                1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    public static void strike(ServerLevel server, Mob attacker, LivingEntity target,
                              float damage, double push) {
        if (target.hurtServer(server, server.damageSources().mobAttack(attacker), damage) && push > 0) {
            double x = target.getX() - attacker.getX();
            double z = target.getZ() - attacker.getZ();
            double length = Math.max(0.01D, Math.sqrt(x * x + z * z));
            target.push(x / length * push, Math.min(0.42D, push * 0.35D), z / length * push);
        }
    }

    public static void step(Mob mob, String machine, int period) {
        if (mob.onGround() && mob.getDeltaMovement().horizontalDistanceSqr() > 0.008D
            && mob.tickCount % period == 0) cue(mob, machine, "step", 0.55F);
    }
}
