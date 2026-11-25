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
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** V2 Scrap Crawler: hostile mechanical hunter with recurring pursuit overclock. */
public final class ScrapCrawlerEntity extends Spider {
    private int appliedStage = -1;
    private boolean appliedBoost;

    public ScrapCrawlerEntity(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        // Lasts 30 ticks per 130 ticks while actively chasing a target.
        boolean sprint = getTarget() != null && distanceToSqr(getTarget()) < 196.0D
            && tickCount % 130 < 30;
        int stage = ReworkCombat.stage(server);
        if (stage != appliedStage || sprint != appliedBoost) {
            ReworkCombat.apply(this, stage, 16.0D, 3.0D, 0.31D, 2.0D, sprint);
            appliedStage = stage;
            appliedBoost = sprint;
        }
    }
}
