package com.unityindustry.entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
/** Hostile at all times, including daylight; CaveSpider poison melee baseline. */
public final class CrystalBeetleEntity extends CaveSpider {
    private boolean scaled;
    public CrystalBeetleEntity(EntityType<? extends CaveSpider> type, Level level) { super(type, level); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (scaled || !(level() instanceof ServerLevel world)) return;
        scaled = true;
        double factor = WorldStageRules.scrapCrawlerMultiplier(UnityProgress.worldStage(world.getServer()));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(18.0 * factor);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0 * factor);
        setHealth((float)(18.0 * factor));
    }
}
