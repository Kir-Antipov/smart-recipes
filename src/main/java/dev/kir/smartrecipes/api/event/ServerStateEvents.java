package dev.kir.smartrecipes.api.event;

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

    public static final Event<GameModeChanged> GAMEMODE_CHANGED = EventFactory.createArrayBacked(GameModeChanged.class, callbacks -> (server, oldGameMode, newGameMode) -> {
        for (GameModeChanged callback : callbacks) {
            callback.onGameModeChanged(server, oldGameMode, newGameMode);
        }
    });

    @FunctionalInterface
    public interface DifficultyChanged {
        void onDifficultyChanged(MinecraftServer server, Difficulty oldDifficulty, Difficulty newDifficulty);
    }

    @FunctionalInterface
    public interface GameModeChanged {
        void onGameModeChanged(MinecraftServer server, GameMode oldGameMode, GameMode newGameMode);
    }
}