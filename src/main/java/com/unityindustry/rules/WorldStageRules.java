package com.unityindustry.rules;

/** Provisional tier balance. Only the server-wide maximum progression rule is final. */
public final class WorldStageRules {
    public static int clamp(int stage) { return Math.max(0, Math.min(4, stage)); }
    public static double scrapCrawlerMultiplier(int stage) {
        return switch (clamp(stage)) {
            case 1 -> 1.15;
            case 2 -> 1.30;
            case 3 -> 1.55;
            case 4 -> 1.80;
            default -> 1.00;
        };
    }
    public static int maxEver(int previous, int incoming) {
        return Math.max(clamp(previous), clamp(incoming));
    }
    private WorldStageRules() { }
}
