package dev.kirantipov.smartrecipes.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RecipeCondition {
    default Identifier getId() {
        return RecipeConditions.REGISTRY.getId(this);
    }

    default boolean testConditionBody(JsonObject conditionBody, RecipeInfo info) {
        if (conditionBody == null) {
            throw new IllegalArgumentException("'conditionBody' cannot be null");
        }
        return test(conditionBody.get("args"), info);
    }

    boolean test(@Nullable JsonElement args, RecipeInfo info);


    static boolean isConditionBody(JsonElement jsonElement) {
        return jsonElement instanceof JsonObject jsonObject && JsonHelper.hasString(jsonObject, "condition");
    }

    static boolean test(JsonObject conditionBody, RecipeInfo info) {
        if (conditionBody == null) {
            throw new IllegalArgumentException("'conditionBody' cannot be null");
        }
        String id = JsonHelper.getString(conditionBody, "condition");
        RecipeCondition condition = get(id);
        if (condition == null) {
            throw new IllegalArgumentException("Unknown condition " + id);
        }

        return condition.testConditionBody(conditionBody, info);
    }

    static boolean test(String id, @Nullable JsonElement args, RecipeInfo info) {
        return test(new Identifier(id), args, info);
    }

    static boolean test(Identifier id, @Nullable JsonElement args, RecipeInfo info) {
        RecipeCondition condition = get(id);
        if (condition == null) {
            throw new IllegalArgumentException("Unknown condition " + id);
        }
        return condition.test(args, info);
    }

    @Nullable
    static RecipeCondition get(String id) {
        return get(new Identifier(id));
    }

    @Nullable
    static RecipeCondition get(Identifier id) {
        return RecipeConditions.REGISTRY.get(id);
    }

    static <T extends RecipeCondition> T register(String id, T condition) {
        return register(new Identifier(id), condition);
    }

    static <T extends RecipeCondition> T register(Identifier id, T condition) {
        return Registry.register(RecipeConditions.REGISTRY, id, condition);
    }
}