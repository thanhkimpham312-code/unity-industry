package com.unityindustry.entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
/** Hostile flying drone prototype; Blaze locomotion/projectile baseline. */
public final class MiningDroneEntity extends Blaze {
    private boolean scaled;
    public MiningDroneEntity(EntityType<? extends Blaze> type, Level level) { super(type, level); }
    @Override public void tick() {
        super.tick();
        if (scaled || !(level() instanceof ServerLevel world)) return;
        scaled = true;
        double factor = WorldStageRules.scrapCrawlerMultiplier(UnityProgress.worldStage(world.getServer()));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(16.0 * factor);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0 * factor);
        setHealth((float)(16.0 * factor));
    }
}
