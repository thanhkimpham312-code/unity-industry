package com.unityindustry.entity;

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
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;

/** Hostile prototype with spider geometry and explicit pursuit goals.
 *  Unlike Vanilla Spider, it never switches to daylight-neutral targeting.
 */
public final class ScrapCrawlerEntity extends Spider {
    private boolean stageApplied = false;
    @Override public void tick() {
        super.tick();
        if (stageApplied || level().isClientSide() || getServer() == null) return;
        stageApplied = true;
        double m = WorldStageRules.scrapCrawlerMultiplier(UnityProgress.worldStage(getServer()));
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (damage != null) damage.setBaseValue(3.0D * m);
        if (health != null) {
            health.setBaseValue(16.0D * m);
            setHealth((float)(16.0D * m));
        }
    }

    public ScrapCrawlerEntity(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        // Do not call Spider#registerGoals: its target goal can stop in daylight.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,
            new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
