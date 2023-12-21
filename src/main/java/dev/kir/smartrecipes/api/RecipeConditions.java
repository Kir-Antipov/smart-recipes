package dev.kir.smartrecipes.api;

import com.google.gson.*;
import com.mojang.serialization.Lifecycle;
import dev.kir.smartrecipes.SmartRecipes;
import dev.kir.smartrecipes.util.JsonUtil;
import dev.kir.smartrecipes.util.world.TimeOfDay;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class RecipeConditions {
    public static final RegistryKey<Registry<RecipeCondition>> KEY = RegistryKey.ofRegistry(SmartRecipes.locate("recipe_condition"));
    public static final Registry<RecipeCondition> REGISTRY = new SimpleRegistry<>(KEY, Lifecycle.experimental());


    public static final RecipeCondition FALSE = (e, i) -> false;

    public static final RecipeCondition TRUE = (e, i) -> true;


    public static final RecipeCondition COMMENT = (e, i) -> true;

    public static final RecipeCondition _COMMENT = (e, i) -> false;


    public static final RecipeCondition OR = (e, i) -> {
        Iterator<Boolean> iterator = test(e, i).iterator();
        if (!iterator.hasNext()) {
            throw new JsonSyntaxException("The OR operator requires at least one argument. Consider using ANY instead.");
        }

        do {
            if (iterator.next()) {
                return true;
            }
        } while (iterator.hasNext());

        return false;
    };

    public static final RecipeCondition AND = (e, i) -> {
        Iterator<Boolean> iterator = test(e, i).iterator();
        if (!iterator.hasNext()) {
            throw new JsonSyntaxException("The AND operator requires at least one argument. Consider using ALL instead.");
        }

        do {
            if (!iterator.next()) {
                return false;
            }
        } while (iterator.hasNext());

        return true;
    };

    public static final RecipeCondition NOT = (e, i) -> {
        Iterator<Boolean> iterator = test(e, i).iterator();
        if (!iterator.hasNext()) {
            throw new JsonSyntaxException("The NOT operator requires at least one argument. Consider using NONE instead.");
        }

        do {
            if (iterator.next()) {
                return false;
            }
        } while (iterator.hasNext());

        return true;
    };


    public static final RecipeCondition ANY = (e, i) -> test(e, i).anyMatch(x -> x);

    public static final RecipeCondition ALL = (e, i) -> test(e, i).allMatch(x -> x);

    public static final RecipeCondition NONE = (e, i) -> test(e, i).noneMatch(x -> x);


    public static final ContextualRecipeCondition IS_HARDCORE = (e, ctx) -> ctx.getServer().isHardcore();

    public static final ContextualRecipeCondition DIFFICULTY_CHECK = (e, ctx) -> JsonUtil.flatMap(e).map(RecipeConditions::parseDifficulty).anyMatch(ctx.getServer().getSaveProperties().getDifficulty()::equals);

    public static final ContextualRecipeCondition IS_PEACEFUL_DIFFICULTY = (e, ctx) -> Difficulty.PEACEFUL.equals(ctx.getServer().getSaveProperties().getDifficulty());

    public static final ContextualRecipeCondition IS_EASY_DIFFICULTY = (e, ctx) -> Difficulty.EASY.equals(ctx.getServer().getSaveProperties().getDifficulty());

    public static final ContextualRecipeCondition IS_NORMAL_DIFFICULTY = (e, ctx) -> Difficulty.NORMAL.equals(ctx.getServer().getSaveProperties().getDifficulty());

    public static final ContextualRecipeCondition IS_HARD_DIFFICULTY = (e, ctx) -> Difficulty.HARD.equals(ctx.getServer().getSaveProperties().getDifficulty());


    public static final ContextualRecipeCondition GAMEMODE_CHECK = (e, ctx) -> JsonUtil.flatMap(e).map(RecipeConditions::parseGameMode).anyMatch(ctx.getServer().getDefaultGameMode()::equals);

    public static final ContextualRecipeCondition IS_SURVIVAL = (e, ctx) -> GameMode.SURVIVAL.equals(ctx.getServer().getDefaultGameMode());

    public static final ContextualRecipeCondition IS_CREATIVE = (e, ctx) -> GameMode.CREATIVE.equals(ctx.getServer().getDefaultGameMode());

    public static final ContextualRecipeCondition IS_ADVENTURE = (e, ctx) -> GameMode.ADVENTURE.equals(ctx.getServer().getDefaultGameMode());

    public static final ContextualRecipeCondition IS_SPECTATOR = (e, ctx) -> GameMode.SPECTATOR.equals(ctx.getServer().getDefaultGameMode());


    public static final ContextualRecipeCondition WEATHER_CHECK = (e, ctx) -> {
        boolean clear = false;
        boolean rain = false;
        boolean thunder = false;

        for (String weatherId : (Iterable<String>)JsonUtil.flatMap(e).map(JsonPrimitive::getAsString)::iterator) {
            weatherId = weatherId.toLowerCase();
            if (weatherId.startsWith("clea")) {
                clear = true;
            } else if (weatherId.startsWith("rain")) {
                rain = true;
            } else if (weatherId.startsWith("thunder")) {
                thunder = true;
            }
        }

        WorldProperties worldProperties = ctx.getServer().getOverworld().getLevelProperties();
        return (
            clear && !worldProperties.isRaining() ||
            rain && worldProperties.isRaining() ||
            thunder && worldProperties.isThundering()
        );
    };

    public static final ContextualRecipeCondition TIME_CHECK = (e, ctx) -> {
        TimeOfDay time = TimeOfDay.fromTime(ctx.getServer().getOverworld().getTimeOfDay());
        return JsonUtil.flatMap(e).map(RecipeConditions::parseTimeOfDay).anyMatch(x -> x.contains(time));
    };


    public static final ContextualRecipeCondition PLAYERS_ONLINE = (e, ctx) -> {
        List<String> names = Arrays.asList(ctx.getServer().getPlayerNames());
        return JsonUtil.flatMap(e).allMatch(x -> names.contains(x.getAsString()));
    };


    public static final RecipeCondition ENTRIES_REGISTERED = (e, i) -> JsonUtil.flatMap(e, RegistryEntry::parse, RegistryEntry::parse).allMatch(x -> {
        Registry<?> registry = Registries.REGISTRIES.get(new Identifier(x.registry()));
        return registry != null && registry.containsId(new Identifier(x.entry()));
    });

    public static final RecipeCondition BLOCKS_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registries.BLOCK.containsId(new Identifier(x.getAsString())));

    public static final RecipeCondition ITEMS_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registries.ITEM.containsId(new Identifier(x.getAsString())));

    public static final RecipeCondition BLOCK_ENTITIES_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registries.BLOCK_ENTITY_TYPE.containsId(new Identifier(x.getAsString())));


    public static final RecipeCondition MODS_LOADED = (e, i) -> JsonUtil.flatMap(e, ModEntry::parse, ModEntry::parse).allMatch(x -> {
        ModContainer mod = FabricLoader.getInstance().getModContainer(x.id()).orElse(null);
        if (mod == null) {
            return false;
        }

        try {
            return VersionPredicate.parse(x.version()).test(mod.getMetadata().getVersion());
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    });


    private static Stream<Boolean> test(JsonElement element, RecipeInfo info) {
        if (RecipeCondition.isConditionBody(element)) {
            return Stream.of(RecipeCondition.test((JsonObject)element, info));
        }

        if (element instanceof JsonObject jsonObject) {
            return jsonObject.entrySet().stream().map(entry -> RecipeCondition.test(entry.getKey(), entry.getValue(), info));
        }

        if (element instanceof JsonArray jsonArray) {
            return StreamSupport.stream(jsonArray.spliterator(), false).flatMap(e -> test(e, info));
        }

        if (element instanceof JsonPrimitive primitive && primitive.isString()) {
            return Stream.of(RecipeCondition.test(primitive.getAsString(), null, info));
        }

        return Stream.of(JsonUtil.asBoolean(element));
    }

    private static Difficulty parseDifficulty(JsonPrimitive jsonPrimitive) {
        return jsonPrimitive.isNumber() ? Difficulty.byId(jsonPrimitive.getAsInt()) : Difficulty.byName(jsonPrimitive.getAsString().toLowerCase());
    }

    private static GameMode parseGameMode(JsonPrimitive jsonPrimitive) {
        return jsonPrimitive.isNumber() ? GameMode.byId(jsonPrimitive.getAsInt()) : GameMode.byName(jsonPrimitive.getAsString().toLowerCase());
    }

    private static TimeOfDay parseTimeOfDay(JsonPrimitive jsonPrimitive) {
        return jsonPrimitive.isNumber() ? TimeOfDay.fromTime(jsonPrimitive.getAsLong()) : TimeOfDay.byName(jsonPrimitive.getAsString().toLowerCase());
    }
}