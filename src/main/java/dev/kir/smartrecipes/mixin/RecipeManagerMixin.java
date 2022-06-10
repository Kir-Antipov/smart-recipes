package dev.kir.smartrecipes.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kir.smartrecipes.api.*;
import dev.kir.smartrecipes.util.JsonUtil;
import dev.kir.smartrecipes.SmartRecipes;
import dev.kir.smartrecipes.api.networking.SynchronizeReloadedRecipesPacket;
import dev.kir.smartrecipes.util.recipe.RecipeBookUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
class RecipeManagerMixin implements ReloadableRecipeManager {
    @Unique
    private static final Identifier CONDITIONS = SmartRecipes.locate("conditions");
    @Unique
    private static final Identifier OBSOLETE_CONDITIONS = new Identifier("conditions");

    @Unique
    private static final Identifier RELOAD_CONDITIONS = SmartRecipes.locate("reload_conditions");
    @Unique
    private static final Identifier OBSOLETE_RELOAD_CONDITIONS = new Identifier("reload_conditions");

    @Shadow
    private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

    @Unique
    private Map<Identifier, Collection<Map.Entry<Identifier, JsonElement>>> waitingForReload;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
    private void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        this.waitingForReload = new ConcurrentHashMap<>();

        int skipped = 0;
        Identifier defaultContextualConditionId = RecipeReloadCondition.END_DATA_PACK_RELOAD.getId();

        Iterator<Map.Entry<Identifier, JsonElement>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Identifier, JsonElement> entry = iterator.next();
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            Identifier recipeId = entry.getKey();
            JsonObject recipeObject = entry.getValue().getAsJsonObject();
            JsonElement conditions = JsonUtil.get(recipeObject, CONDITIONS);
            boolean isObsoleteEntry = false;
            if (conditions == null) {
                conditions = JsonUtil.get(recipeObject, OBSOLETE_CONDITIONS);
                isObsoleteEntry = true;
            }
            if (conditions == null) {
                continue;
            }

            Stream<Identifier> reloadConditions = Stream.empty();
            try {
                if (!RecipeCondition.ALL.test(conditions, new RecipeInfo(recipeId, recipeObject))) {
                    iterator.remove();
                    ++skipped;
                }
            } catch (RecipeContextRequiredException e) {
                reloadConditions = Stream.of(defaultContextualConditionId);
            } catch (Throwable e) {
                if (!isObsoleteEntry) {
                    logParsingError(recipeId, e);
                }
                isObsoleteEntry = false;
            }

            JsonElement definedReloadConditions = JsonUtil.get(recipeObject, RELOAD_CONDITIONS);
            if (definedReloadConditions == null) {
                definedReloadConditions = JsonUtil.get(recipeObject, OBSOLETE_RELOAD_CONDITIONS);
                isObsoleteEntry |= definedReloadConditions != null;
            }

            reloadConditions = Stream.concat(reloadConditions, JsonUtil.flatMap(definedReloadConditions).map(x -> Identifier.tryParse(x.getAsString())).filter(Objects::nonNull));
            for (Identifier reloadConditionId : (Iterable<Identifier>)reloadConditions::iterator) {
                this.waitingForReload.computeIfAbsent(reloadConditionId, i -> ConcurrentHashMap.newKeySet()).add(entry);
            }

            if (isObsoleteEntry && SmartRecipes.CONFIG.getValue("debug").orElse(true)) {
                SmartRecipes.LOGGER.warn("{} uses obsolete '{}' and/or '{}' entries. Consider using '{}' and '{}' instead.", recipeId, OBSOLETE_CONDITIONS, OBSOLETE_RELOAD_CONDITIONS, CONDITIONS, RELOAD_CONDITIONS);
            }
        }

        SmartRecipes.LOGGER.info("Skipped {} recipes", skipped);
    }

    @Override
    public void reload(MinecraftServer server, Identifier cause) {
        Collection<Map.Entry<Identifier, JsonElement>> reloadableRecipes = this.waitingForReload == null ? null : this.waitingForReload.get(cause);
        if (server == null || reloadableRecipes == null || reloadableRecipes.size() == 0) {
            return;
        }

        int reloaded = 0;
        int removed = 0;
        int added = 0;
        List<Pair<RecipeState, RecipeInfo>> diff = new ArrayList<>();
        RecipeContext recipeContext = new RecipeContext(server, server.getResourceManager(), server.getProfiler());
        for (Map.Entry<Identifier, JsonElement> entry : reloadableRecipes) {
            Identifier recipeId = entry.getKey();
            try {
                JsonObject recipeObject = entry.getValue().getAsJsonObject();
                RecipeInfo recipeInfo = recipeContext.with(recipeId, recipeObject);
                JsonElement conditions = JsonUtil.get(recipeObject, CONDITIONS);
                if (conditions == null) {
                    conditions = JsonUtil.get(recipeObject, OBSOLETE_CONDITIONS);
                }

                boolean shouldExist = RecipeCondition.ALL.test(conditions, recipeInfo);
                boolean exists = this.recipes.get(recipeInfo.getRecipeType().orElseThrow()).containsKey(recipeId);
                if (shouldExist != exists) {
                    diff.add(new Pair<>(shouldExist ? RecipeState.KEEP : RecipeState.REMOVE, recipeInfo));
                    ++reloaded;
                    if (shouldExist) {
                        ++added;
                    } else {
                        ++removed;
                    }
                }
            } catch (Throwable e) {
                logParsingError(recipeId, e);
            }
        }

        if (reloaded != 0) {
            this.apply(diff);
            this.sync(server, diff);
            SmartRecipes.LOGGER.info("Reloaded {} recipes (removed: {} | added: {}) on the '{}' event", reloaded, removed, added, cause);
        }
    }

    @Override
    public void apply(Collection<Pair<RecipeState, RecipeInfo>> diff) {
        if (diff.isEmpty()) {
            return;
        }

        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> mutableRecipes = makeMutable(this.recipes);
        for (Pair<RecipeState, RecipeInfo> entry : diff) {
            RecipeState recipeState = entry.getLeft();
            RecipeInfo recipeInfo = entry.getRight();
            Identifier recipeId = recipeInfo.getRecipeId();
            try {
                RecipeType<?> recipeType = recipeInfo.getRecipeType().orElseThrow(() -> new IllegalArgumentException("Recipe '" + recipeId + "' uses invalid or unsupported recipe type"));
                switch (recipeState) {
                    case KEEP -> {
                        Map<Identifier, Recipe<?>> container = mutableRecipes.computeIfAbsent(recipeType, x -> new HashMap<>());
                        container.put(recipeId, recipeInfo.getRecipe().orElseThrow(() -> new IllegalArgumentException("Unable to parse recipe '" + recipeId + "'")));
                    }
                    case REMOVE -> {
                        Map<Identifier, Recipe<?>> container = mutableRecipes.get(recipeType);
                        if (container != null) {
                            container.remove(recipeId);
                        }
                    }
                }
            } catch (Throwable e) {
                logParsingError(recipeId, e);
            }
        }
        this.recipes = mutableRecipes;
    }

    @Unique
    private void sync(MinecraftServer server, Collection<Pair<RecipeState, RecipeInfo>> diff) {
        if (diff.isEmpty()) {
            return;
        }

        new SynchronizeReloadedRecipesPacket(diff).send(PlayerLookup.all(server).stream());
        PlayerLookup.all(server).forEach(x -> RecipeBookUtil.apply(x.getRecipeBook(), diff));
    }

    @Unique
    private static void logParsingError(Identifier recipeId, Throwable error) {
        if (SmartRecipes.CONFIG.getValue("debug").orElse(true)) {
            SmartRecipes.LOGGER.error("Parsing error loading recipe {}", recipeId, error);
        }
    }

    @Unique
    private static Map<RecipeType<?>, Map<Identifier, Recipe<?>>> makeMutable(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes) {
        if (recipes instanceof HashMap<?, ?>) {
            return recipes;
        }

        return recipes.entrySet().stream().map(x -> new AbstractMap.SimpleEntry<>(x.getKey(), new HashMap<>(x.getValue()))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}