package com.unityindustry.entity;

import com.unityindustry.sound.ReworkSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

/** Mechanical crystal armor: shell mode, shrapnel pulse, short telegraphed ram. */
public final class CrystalBeetleEntity extends Spider {
    private int appliedStage = -1, shellTicks = 0, shellCooldown = 0;
    private int burstCharge = 0, burstCooldown = 70, ramCharge = 0, ramCooldown = 60, ramActive = 0;
    private LivingEntity ramTarget;
    public CrystalBeetleEntity(EntityType<? extends Spider> type, Level level) { super(type, level); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        if (stage != appliedStage) {
            ReworkCombat.apply(this, stage, 20, 3, 0.25, 7, false);
            appliedStage = stage;
            if (shellTicks > 0) {
                var armor = getAttribute(Attributes.ARMOR);
                if (armor != null) armor.setBaseValue(22D * com.unityindustry.rules.WorldStageRules.scrapCrawlerMultiplier(stage));
            }
        }
        ReworkActions.step(this, "crystal_beetle", 13);
        if (shellCooldown > 0) shellCooldown--;
        if (ramCooldown > 0) ramCooldown--;
        if (burstCooldown > 0) burstCooldown--;
        if (shellTicks > 0) {
            shellTicks--;
            if (shellTicks % 8 == 0) ReworkActions.warning(server, this, 4);
            if (shellTicks == 0) {
                var armor = getAttribute(Attributes.ARMOR);
                if (armor != null) armor.setBaseValue(7D * com.unityindustry.rules.WorldStageRules.scrapCrawlerMultiplier(stage));
            }
        } else if (shellCooldown == 0 && getHealth() < getMaxHealth() * 0.55F && getTarget() != null) {
            shellTicks = 40; shellCooldown = 300;
            var armor = getAttribute(Attributes.ARMOR);
            if (armor != null) armor.setBaseValue(22D * com.unityindustry.rules.WorldStageRules.scrapCrawlerMultiplier(stage));
            ReworkActions.cue(this, "crystal_beetle", "special", 0.95F);
        }
        LivingEntity target = getTarget();
        if (ramCharge > 0) {
            ramCharge--;
            if (ramCharge == 0 && ReworkActions.canHit(this, ramTarget, 5.5D)) {
                double dx = ramTarget.getX() - getX(), dz = ramTarget.getZ() - getZ();
                double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                push(dx / len * 0.8D, 0.19D, dz / len * 0.8D);
                ramActive = 9;
                ReworkActions.cue(this, "crystal_beetle", "attack", 1.0F);
            }
        } else if (ramActive > 0) {
            ramActive--;
            if (ReworkActions.canHit(this, ramTarget, 2.0D)) {
                ReworkActions.strike(server, this, ramTarget, 3.5F + stage, 0.35D);
                ramActive = 0;
            }
        } else if (burstCharge > 0) {
            burstCharge--;
            if (burstCharge % 4 == 0) ReworkActions.warning(server, this, 6);
            if (burstCharge == 0) {
                ReworkActions.cue(this, "crystal_beetle", "attack", 1.0F);
                ReworkActions.impact(server, this, 26);
                for (Player p : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(3.2D))) {
                    if (ReworkActions.canHit(this, p, 3.2D)) ReworkActions.strike(server, this, p, 3.5F + stage, 0.36D);
                }
            }
        } else if (burstCooldown == 0 && ReworkActions.canHit(this, target, 3.2D)) {
            burstCharge = 20; burstCooldown = 240;
            ReworkActions.cue(this, "crystal_beetle", "charge", 1.0F);
        } else if (ramCooldown == 0 && ReworkActions.canHit(this, target, 5.5D)) {
            ramCharge = 16; ramCooldown = 140; ramTarget = target;
            ReworkActions.cue(this, "crystal_beetle", "charge", 0.85F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return ReworkSounds.cue("crystal_beetle", "ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ReworkSounds.cue("crystal_beetle", "hurt"); }
    @Override protected SoundEvent getDeathSound() { return ReworkSounds.cue("crystal_beetle", "death"); }
}
