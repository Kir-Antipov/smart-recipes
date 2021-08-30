package dev.kirantipov.smartrecipes.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public final class ServerStateEvents {
    private ServerStateEvents() { }

    public static final Event<DifficultyChanged> DIFFICULTY_CHANGED = EventFactory.createArrayBacked(DifficultyChanged.class, callbacks -> (server, oldDifficulty, newDifficulty) -> {
        for (DifficultyChanged callback : callbacks) {
            callback.onDifficultyChanged(server, oldDifficulty, newDifficulty);
        }
    });

    @FunctionalInterface
    public interface DifficultyChanged {
        void onDifficultyChanged(MinecraftServer server, Difficulty oldDifficulty, Difficulty newDifficulty);
    }
}