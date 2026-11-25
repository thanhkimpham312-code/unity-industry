package com.unityindustry.sound;

import com.unityindustry.UnityIndustry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Registered industrial sounds; each cue has its own original OGG asset. */
public final class ReworkSounds {
    private static final Map<String, SoundEvent> EVENTS = new HashMap<>();
    private static final String[] MACHINES = {
        "scrap_crawler", "mining_drone", "crystal_beetle",
        "security_sentinel", "excavator_mk1"
    };
    private static final String[] CUES = {
        "ambient", "step", "alert", "charge", "attack", "special", "hurt", "death"
    };

    static {
        for (String machine : MACHINES) {
            for (String cue : CUES) {
                String path = "mob." + machine + "." + cue;
                Identifier id = UnityIndustry.id(path);
                EVENTS.put(machine + ":" + cue, Registry.register(
                    BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
            }
        }
    }

    public static SoundEvent cue(String machine, String cue) {
        SoundEvent event = EVENTS.get(machine + ":" + cue);
        if (event == null) throw new IllegalArgumentException("Unknown machine sound " + machine + ":" + cue);
        return event;
    }
    public static void initialize() { /* force static registration before registry freeze */ }
    private ReworkSounds() { }
}
