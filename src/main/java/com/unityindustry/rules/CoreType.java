package com.unityindustry.rules;

/** Approved Core names and Research Desk secondary ingredients. */
public enum CoreType {
    FIRE("minecraft:magma_cream", 1),
    FLYING("minecraft:elytra", 1),
    ORE_SCANNER("minecraft:echo_shard", 1),
    EXCAVATION("minecraft:netherite_pickaxe", 1),
    MAGNET("minecraft:lodestone", 1),
    PRESSURE("minecraft:conduit", 1),
    AGRICULTURE("minecraft:bone_meal", 1),
    REFINERY("minecraft:blast_furnace", 1),
    STORAGE("minecraft:shulker_shell", 1),
    MAINTENANCE("minecraft:anvil", 1),
    ULTRA("unityindustry:prime_core", 3);

    private final String ingredientId;
    private final int slots;
    CoreType(String ingredientId, int slots) {
        this.ingredientId = ingredientId;
        this.slots = slots;
    }
    public String ingredientId() { return ingredientId; }
    public int slots() { return slots; }
}
