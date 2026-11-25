package com.unityindustry.entity;

import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.rules.WorldStageRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Server-only combat profile for reworked V2 entities.
 * Preserves current HP ratio when the shared progression stage changes.
 */
public final class ReworkCombat {
    private ReworkCombat() { }

    public static int stage(ServerLevel level) {
        return UnityProgress.worldStage(level.getServer());
    }

    public static void apply(Mob mob, int worldStage, double baseHealth,
                             double baseDamage, double baseSpeed, double baseArmor,
                             boolean overclock) {
        double factor = WorldStageRules.scrapCrawlerMultiplier(worldStage);
        float oldMax = mob.getMaxHealth();
        float healthRatio = oldMax > 0 ? mob.getHealth() / oldMax : 1.0f;
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(baseHealth * factor);
        var damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(baseDamage * factor * (overclock ? 1.15d : 1.0d));
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(baseSpeed * (overclock ? 1.55d : 1.0d));
        var armor = mob.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(baseArmor * factor);
        float targetHp = mob.getMaxHealth() * Math.max(0.0f, Math.min(1.0f, healthRatio));
        mob.setHealth(Math.min(mob.getMaxHealth(), Math.max(0.0f, targetHp)));
    }
}
