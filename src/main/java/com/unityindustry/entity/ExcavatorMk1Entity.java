package com.unityindustry.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** V2 Excavator MK-I: heavy hostile drilling machine with a server-side shockwave. */
public final class ExcavatorMk1Entity extends IronGolem {
    private int appliedStage = -1;
    private boolean boosted;

    public ExcavatorMk1Entity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        boolean overclock = getTarget() != null && getHealth() < getMaxHealth() * 0.35F;
        if (stage != appliedStage || boosted != overclock) {
            ReworkCombat.apply(this, stage, 90.0D, 11.0D, 0.20D, 12.0D, overclock);
            appliedStage = stage;
            boosted = overclock;
        }
        // Only targets players; damage is never applied client-side.
        if (getTarget() != null && tickCount % 145 == 0) {
            for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(3.5D))) {
                if (player.isCreative() || player.isSpectator() || distanceToSqr(player) > 12.25D) continue;
                if (player.hurtServer(server, server.damageSources().mobAttack(this), 3.0F + stage)) {
                    double dx = player.getX() - getX();
                    double dz = player.getZ() - getZ();
                    double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                    player.push(dx / len * 0.85D, 0.43D, dz / len * 0.85D);
                }
            }
        }
    }
}
