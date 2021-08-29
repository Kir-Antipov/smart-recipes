package dev.kirantipov.smartrecipes.api;

import com.google.gson.*;
import com.mojang.serialization.Lifecycle;
import dev.kirantipov.smartrecipes.util.JsonUtil;
import dev.kirantipov.smartrecipes.util.world.TimeOfDay;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.util.version.VersionPredicateParser;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class RecipeConditions {
    public static final RegistryKey<Registry<RecipeCondition>> KEY = RegistryKey.ofRegistry(new Identifier("recipe_condition"));
    public static final Registry<RecipeCondition> REGISTRY = new SimpleRegistry<>(KEY, Lifecycle.experimental());


    public static final RecipeCondition FALSE = (e, i) -> false;

    public static final RecipeCondition TRUE = (e, i) -> true;


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


    public static final RecipeCondition ENTRIES_REGISTERED = (e, i) -> JsonUtil.flatMap(e, RegistryEntry::parse, RegistryEntry::parse).allMatch(x -> {
        Registry<?> registry = Registry.REGISTRIES.get(new Identifier(x.registry()));
        return registry != null && registry.containsId(new Identifier(x.entry()));
    });

    public static final RecipeCondition BLOCKS_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registry.BLOCK.containsId(new Identifier(x.getAsString())));

    public static final RecipeCondition ITEMS_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registry.ITEM.containsId(new Identifier(x.getAsString())));

    public static final RecipeCondition BLOCK_ENTITIES_REGISTERED = (e, i) -> JsonUtil.flatMap(e).allMatch(x -> Registry.BLOCK_ENTITY_TYPE.containsId(new Identifier(x.getAsString())));


    public static final RecipeCondition MODS_LOADED = (e, i) -> JsonUtil.flatMap(e, ModEntry::parse, ModEntry::parse).allMatch(x -> {
        ModContainer mod = FabricLoader.getInstance().getModContainer(x.id()).orElse(null);
        if (mod == null) {
            return false;
        }

        try {
            return VersionPredicateParser.matches(mod.getMetadata().getVersion(), x.version());
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
}