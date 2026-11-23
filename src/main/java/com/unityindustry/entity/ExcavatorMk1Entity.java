package com.unityindustry.entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;
/** Hostile industrial heavy enemy prototype. IronGolem hit geometry and slam attack. */
public final class ExcavatorMk1Entity extends IronGolem {
    private boolean scaled;
    public ExcavatorMk1Entity(EntityType<? extends IronGolem> type, Level world) { super(type, world); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.10D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.80D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (scaled || !(level() instanceof ServerLevel world)) return;
        scaled = true;
        double factor = WorldStageRules.scrapCrawlerMultiplier(UnityProgress.worldStage(world.getServer()));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(90.0 * factor);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(11.0 * factor);
        setHealth((float)(90.0 * factor));
    }
}
