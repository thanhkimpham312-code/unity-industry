package com.unityindustry.entity;

/** Names and combat values are PROTOTYPE VALUES, not decisions approved by the project owner. */
public record MachineSpec(String id, int minStage, double health, double attack,
                          double speed, double armor, float width, float height,
                          int spawnWeight, boolean boss) {
}
