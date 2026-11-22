package com.unityindustry.rules;

/**
 * Non-negative, non-capped Unity Energy model. Uses long instead of int.
 * Persist it per player in later network/server milestone.
 */
public final class EnergyLedger {
    public static final long UE_PER_INGOT = 100L;
    public static final int CHARGE_INTERVAL_TICKS = 4; // 0.2 seconds at 20 TPS
    public static final long EXCAVATION_PER_BLOCK = 1L;
    public static final long AGRICULTURE_PER_CROP = 2L;
    public static final long REPAIR_PER_DURABILITY = 1L;
    public static final long ULTRA_WITHER_ROSES = 10L;
    public static final long ULTRA_WITHER_SKULL = 5L;
    public static final long ULTRA_WITHER_HIT = 0L;
    public static final long CONTINUOUS_UE_PER_SECOND = 2L;

    private long amount;
    public EnergyLedger(long initial) {
        if (initial < 0) throw new IllegalArgumentException("Energy cannot be negative");
        this.amount = initial;
    }
    public long amount() { return amount; }
    public long chargeIngots(int count) {
        if (count < 0) throw new IllegalArgumentException("Negative ingot count");
        amount = Math.addExact(amount, Math.multiplyExact((long) count, UE_PER_INGOT));
        return amount;
    }
    public boolean consume(long units) {
        if (units < 0) throw new IllegalArgumentException("Negative cost");
        if (units > amount) return false;
        amount -= units;
        return true;
    }
    public static long continuousCostForTicks(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("Negative time");
        // Fractional 2 UE/s is tracked in fixed-point ticks by the eventual server layer.
        // This method calculates the cost for a *completed* duration, rounded up.
        return Math.addExact(Math.multiplyExact(ticks, CONTINUOUS_UE_PER_SECOND), 19L) / 20L;
    }
}
