package com.unityindustry.rules;

import java.util.ArrayList;
import java.util.List;

/** Stable core insertion order; skill numbers are transient, never saved on a chestplate.
 *  A passive / continuously automatic power has no manual key and no cooldown.
 *  If a Core is removed, all later manual skills shift down automatically.
 */
public final class SkillCatalog {
    public enum Action {
        FLIGHT_TOGGLE("Bay", false),
        SCAN_ORES("Quét quặng", false),
        EXCAVATE("Đào 3x3", true),
        OPEN_STORAGE("Kho 9 ô", false),
        WITHER_ROSES("Hoa Wither", false),
        WITHER_SKULL("Đạn Wither", false),
        WHITE_LASER("Laser trắng", true);
        public final String label;
        public final boolean hold;
        Action(String label, boolean hold) { this.label = label; this.hold = hold; }
    }
    public record Entry(CoreType core, Action action) {}

    private static List<Action> forCore(CoreType core) {
        return switch (core) {
            case FLYING -> List.of(Action.FLIGHT_TOGGLE);
            case ORE_SCANNER -> List.of(Action.SCAN_ORES);
            case EXCAVATION -> List.of(Action.EXCAVATE);
            case STORAGE -> List.of(Action.OPEN_STORAGE);
            case ULTRA -> List.of(Action.WITHER_ROSES, Action.FLIGHT_TOGGLE,
                                  Action.WITHER_SKULL, Action.WHITE_LASER);
            default -> List.of(); // FIRE/PRESSURE/MAGNET etc. are automatic, not hotkeys
        };
    }

    public static List<Entry> active(List<CoreType> installed) {
        List<Entry> entries = new ArrayList<>();
        for (CoreType core : installed) {
            for (Action action : forCore(core)) {
                if (entries.size() < 9) entries.add(new Entry(core, action));
            }
        }
        return List.copyOf(entries);
    }
    public static Entry at(List<CoreType> installed, int oneBasedSlot) {
        var active = active(installed);
        return oneBasedSlot > 0 && oneBasedSlot <= active.size()
            ? active.get(oneBasedSlot - 1) : null;
    }
    public static String labels(List<CoreType> installed) {
        var active = active(installed);
        if (active.isEmpty()) return "Không có kỹ năng kích hoạt thủ công";
        var parts = new ArrayList<String>();
        for (int i = 0; i < active.size(); i++) {
            parts.add((i+1) + ": " + active.get(i).action().label);
        }
        return String.join(" | ", parts);
    }
    private SkillCatalog() {}
}
