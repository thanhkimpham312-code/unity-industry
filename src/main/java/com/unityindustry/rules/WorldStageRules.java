package com.unityindustry.rules;

/** World-wide player technology progression retained independently of mobs. */
public final class WorldStageRules {
    public static int clamp(int stage) { return Math.max(0, Math.min(4, stage)); }
    public static int maxEver(int previous, int incoming) {
        return Math.max(clamp(previous), clamp(incoming));
    }
    private WorldStageRules() { }
}
