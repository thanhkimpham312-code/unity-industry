package com.unityindustry.rules;

import java.util.ArrayList;
import java.util.List;

/** Pure gameplay rules; later connected to components and Implant/Reverse menus. */
public final class CoreRules {
    public static final int CHESTPLATE_CAPACITY = 3;
    public enum Status { OK, OVERLOAD, DUPLICATE, ULTRA_REQUIRES_EMPTY, ULTRA_OCCUPIED }
    public record ImplantResult(Status status, List<CoreType> cores) {
        public ImplantResult { cores = List.copyOf(cores); }
        public boolean success() { return status == Status.OK; }
    }

    public static ImplantResult implant(List<CoreType> existing, CoreType next) {
        List<CoreType> before = List.copyOf(existing);
        if (before.contains(next)) return new ImplantResult(Status.DUPLICATE, before);
        if (before.contains(CoreType.ULTRA)) return new ImplantResult(Status.ULTRA_OCCUPIED, before);
        if (next == CoreType.ULTRA && !before.isEmpty())
            return new ImplantResult(Status.ULTRA_REQUIRES_EMPTY, before);
        int used = before.stream().mapToInt(CoreType::slots).sum();
        if (used + next.slots() > CHESTPLATE_CAPACITY)
            return new ImplantResult(Status.OVERLOAD, before);
        List<CoreType> after = new ArrayList<>(before);
        after.add(next);
        return new ImplantResult(Status.OK, after);
    }

    public static List<CoreType> removeOne(List<CoreType> existing, CoreType target) {
        List<CoreType> after = new ArrayList<>(existing);
        if (!after.remove(target)) throw new IllegalArgumentException("Core not installed: " + target);
        return List.copyOf(after);
    }

    public static int slotsUsed(List<CoreType> cores) {
        return cores.stream().mapToInt(CoreType::slots).sum();
    }
    private CoreRules() { }
}
