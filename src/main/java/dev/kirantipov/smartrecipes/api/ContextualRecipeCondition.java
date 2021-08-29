package dev.kirantipov.smartrecipes.api;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ContextualRecipeCondition extends RecipeCondition {
    @Override
    default boolean test(@Nullable JsonElement args, RecipeInfo info) {
        if (info instanceof RecipeContext context) {
            return this.test(args, context);
        }

        throw new RecipeContextRequiredException("Context is required to evaluate this condition");
    }

    boolean test(@Nullable JsonElement data, RecipeContext context);
}