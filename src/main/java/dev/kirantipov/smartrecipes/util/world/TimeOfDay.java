package dev.kirantipov.smartrecipes.util.world;

import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TimeOfDay implements StringIdentifiable {
    DAY("day", 1000, 12999),
    NOON("noon", 5000, 6999),
    SUNSET("sunset", 11000, 12999),
    MIDNIGHT("midnight", 17000, 18999),
    SUNRISE("sunrise", 22000, 23999),
    NIGHT("night", 13000, 23999) {
        @Override
        public boolean contains(long time) {
            return !DAY.contains(time);
        }
    };

    public static final int TICKS_PER_DAY = 24000;
    private static final Map<String, TimeOfDay> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(x -> x.name, x -> x));

    private final String name;
    private final long from;
    private final long to;

    TimeOfDay(String name, long from, long to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public boolean contains(long time) {
        return time >= this.from && time <= this.to;
    }

    public boolean contains(TimeOfDay time) {
        return this.contains(time.from) && this.contains(time.to);
    }

    @Override
    public String asString() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Nullable
    public static TimeOfDay byName(String name) {
        return BY_NAME.get(name);
    }

    public static TimeOfDay fromTime(long time) {
        time = (time + TICKS_PER_DAY) % TICKS_PER_DAY;
        if (DAY.contains(time)) {
            if (NOON.contains(time)) {
                return NOON;
            }

            if (SUNSET.contains(time)) {
                return SUNSET;
            }

            return DAY;
        } else {
            if (MIDNIGHT.contains(time)) {
                return MIDNIGHT;
            }

            if (SUNRISE.contains(time)) {
                return SUNRISE;
            }

            return NIGHT;
        }
    }
}