package dev.kir.smartrecipes.api;

import com.google.gson.JsonObject;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Contract;

public class RecipeContext extends RecipeInfo {
    private final MinecraftServer server;
    private final ResourceManager resourceManager;
    private final Profiler profiler;

    public RecipeContext(MinecraftServer server, ResourceManager resourceManager, Profiler profiler) {
        this(null, null, server, resourceManager, profiler);
    }

    public RecipeContext(Identifier recipeId, JsonObject recipeObject, MinecraftServer server, ResourceManager resourceManager, Profiler profiler) {
        super(recipeId, recipeObject);
        this.server = server;
        this.resourceManager = resourceManager;
        this.profiler = profiler;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public RecipeContext with(Identifier recipeId, JsonObject recipeObject) {
        return new RecipeContext(recipeId, recipeObject, this.server, this.resourceManager, this.profiler);
    }
}