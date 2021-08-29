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
}