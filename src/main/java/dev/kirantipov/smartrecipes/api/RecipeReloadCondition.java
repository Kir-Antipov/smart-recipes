package dev.kirantipov.smartrecipes.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public interface RecipeReloadCondition {
    RecipeReloadCondition END_DATA_PACK_RELOAD = register("end_data_pack_reload", RecipeReloadConditions.END_DATA_PACK_RELOAD);
    RecipeReloadCondition PLAYER_JOINED = register("player_joined", RecipeReloadConditions.PLAYER_JOINED);
    RecipeReloadCondition PLAYER_DISCONNECTED = register("player_disconnected", RecipeReloadConditions.PLAYER_DISCONNECTED);
    RecipeReloadCondition DIFFICULTY_CHANGED = register("difficulty_changed", RecipeReloadConditions.DIFFICULTY_CHANGED);
    RecipeReloadCondition GAMEMODE_CHANGED = register("gamemode_changed", RecipeReloadConditions.GAMEMODE_CHANGED);
    RecipeReloadCondition WEATHER_CHANGED = register("weather_changed", RecipeReloadConditions.WEATHER_CHANGED);
    RecipeReloadCondition TIME_CHANGED = register("time_changed", RecipeReloadConditions.TIME_CHANGED);


    default Identifier getId() {
        return RecipeReloadConditions.REGISTRY.getId(this);
    }

    RecipeReloadConditionListener invoker();

    void register(RecipeReloadConditionListener listener);


    @Nullable
    static RecipeReloadCondition get(String id) {
        return get(new Identifier(id));
    }

    @Nullable
    static RecipeReloadCondition get(Identifier id) {
        return RecipeReloadConditions.REGISTRY.get(id);
    }

    static <T extends RecipeReloadCondition> T register(String id, T condition) {
        return register(new Identifier(id), condition);
    }

    static <T extends RecipeReloadCondition> T register(Identifier id, T condition) {
        condition = Registry.register(RecipeReloadConditions.REGISTRY, id, condition);
        condition.register((server, cause) -> ((ReloadableRecipeManager)server.getRecipeManager()).reload(server, cause));
        return condition;
    }

    @FunctionalInterface
    interface RecipeReloadConditionListener {
        void onRecipeReloadEvent(MinecraftServer server, Identifier cause);
    }
}