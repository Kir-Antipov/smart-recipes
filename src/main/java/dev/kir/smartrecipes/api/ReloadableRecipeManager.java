package dev.kir.smartrecipes.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Collection;

public interface ReloadableRecipeManager {
    void reload(MinecraftServer server, Identifier cause);

    void apply(Collection<Pair<RecipeState, RecipeInfo>> diff);

    enum RecipeState {
        KEEP,
        REMOVE
    }
}