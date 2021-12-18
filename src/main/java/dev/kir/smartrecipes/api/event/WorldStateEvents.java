package dev.kir.smartrecipes.api.event;

import dev.kir.smartrecipes.util.world.TimeOfDay;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

public final class WorldStateEvents {
    private WorldStateEvents() { }

    public static final Event<TimeOfDayChanged> TIME_CHANGED = EventFactory.createArrayBacked(TimeOfDayChanged.class, callbacks -> (world, oldTime, newTime) -> {
        for (TimeOfDayChanged callback : callbacks) {
            callback.onTimeOfDayChanged(world, oldTime, newTime);
        }
    });

    public static final Event<WeatherChanged> WEATHER_CHANGED = EventFactory.createArrayBacked(WeatherChanged.class, callbacks -> world -> {
        for (WeatherChanged callback : callbacks) {
            callback.onWeatherChanged(world);
        }
    });

    @FunctionalInterface
    public interface TimeOfDayChanged {
        void onTimeOfDayChanged(ServerWorld world, TimeOfDay oldTimeOfDay, TimeOfDay newTimeOfDay);
    }

    @FunctionalInterface
    public interface WeatherChanged {
        void onWeatherChanged(ServerWorld world);
    }
}