package com.unityindustry.entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
/** Flying hostile security bot prototype, using ranged Blaze mechanics for now. */
public final class SecuritySentinelEntity extends Blaze {
    private boolean scaled;
    public SecuritySentinelEntity(EntityType<? extends Blaze> type, Level level) { super(type, level); }
    @Override public void tick() {
        super.tick();
        if (scaled || !(level() instanceof ServerLevel world)) return;
        scaled = true;
        double factor = WorldStageRules.scrapCrawlerMultiplier(UnityProgress.worldStage(world.getServer()));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(26.0 * factor);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5.0 * factor);
        setHealth((float)(26.0 * factor));
    }
}
