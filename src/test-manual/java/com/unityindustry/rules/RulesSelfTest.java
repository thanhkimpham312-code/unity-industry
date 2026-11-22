package com.unityindustry.rules;

import java.util.List;

/** Run with scripts/test-core-rules.sh, no Minecraft/Gradle dependencies. */
public final class RulesSelfTest {
    public static void main(String[] args) {
        var one = CoreRules.implant(List.of(), CoreType.FIRE);
        require(one.success(), "first core");
        var two = CoreRules.implant(one.cores(), CoreType.PRESSURE);
        require(two.success(), "second core");
        var three = CoreRules.implant(two.cores(), CoreType.MAGNET);
        require(three.success(), "third core");
        require(CoreRules.slotsUsed(three.cores()) == 3, "three slots");
        require(CoreRules.implant(three.cores(), CoreType.REFINERY).status() == CoreRules.Status.OVERLOAD, "fourth overload");
        require(CoreRules.implant(two.cores(), CoreType.FIRE).status() == CoreRules.Status.DUPLICATE, "duplicate blocked");
        require(CoreRules.implant(two.cores(), CoreType.ULTRA).status() == CoreRules.Status.ULTRA_REQUIRES_EMPTY, "ultra requires empty");
        var ultra = CoreRules.implant(List.of(), CoreType.ULTRA);
        require(ultra.success() && CoreRules.slotsUsed(ultra.cores()) == 3, "ultra fills all");
        require(CoreRules.implant(ultra.cores(), CoreType.FIRE).status() == CoreRules.Status.ULTRA_OCCUPIED, "no mixing ultra");
        require(CoreRules.removeOne(three.cores(), CoreType.PRESSURE).size() == 2, "remove one only");
        EnergyLedger ledger = new EnergyLedger(0);
        ledger.chargeIngots(10);
        require(ledger.amount() == 1000L, "10 ingots is 1000 UE");
        require(ledger.consume(5L) && ledger.amount() == 995L, "consume exact 5");
        require(!ledger.consume(996L) && ledger.amount() == 995L, "no negative energy");
        require(EnergyLedger.continuousCostForTicks(20) == 2L, "2 UE per second");
        require(EnergyLedger.CHARGE_INTERVAL_TICKS == 4, "X hold charge 0.2s");
        System.out.println("PASS: core slots, duplicate, ultra, reverse, charging, and costs");
    }
    private static void require(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
    }
}
