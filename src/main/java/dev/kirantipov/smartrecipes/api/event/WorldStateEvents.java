package dev.kirantipov.smartrecipes.api.event;

import dev.kirantipov.smartrecipes.util.world.TimeOfDay;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

public final class WorldStateEvents {
    private WorldStateEvents() { }

    public static final Event<WeatherChanged> WEATHER_CHANGED = EventFactory.createArrayBacked(WeatherChanged.class, callbacks -> world -> {
        for (WeatherChanged callback : callbacks) {
            callback.onWeatherChanged(world);
        }
    });

    @FunctionalInterface
    public interface WeatherChanged {
        void onWeatherChanged(ServerWorld world);
    }
}