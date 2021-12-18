package dev.kir.smartrecipes.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public class RecipeInfo {
    private final Identifier recipeId;
    private final JsonObject recipeObject;
    private Recipe<?> recipe;
    private RecipeType<?> recipeType;

    public RecipeInfo(Identifier recipeId, JsonObject recipeObject) {
        this.recipeId = recipeId;
        this.recipeObject = recipeObject;
    }

    public Identifier getRecipeId() {
        return this.recipeId;
    }

    public JsonObject getRecipeAsJson() {
        return this.recipeObject;
    }

    public Optional<RecipeType<?>> getRecipeType() {
        if (this.recipe != null) {
            return Optional.of(this.recipe.getType());
        }

        if (this.recipeType == null && this.recipeObject != null) {
            String type = this.recipeObject.get("type") instanceof JsonPrimitive typePrimitive && typePrimitive.isString() ? typePrimitive.getAsString() : null;
            Identifier id = type == null ? null : Identifier.tryParse(type);
            if (id != null) {
                this.recipeType = Registry.RECIPE_TYPE.getOrEmpty(id).or(() -> Registry.RECIPE_TYPE.getOrEmpty(new Identifier(id.getNamespace(), id.getPath().split("_")[0]))).orElse(null);
            }
        }
        return Optional.ofNullable(this.recipeType);
    }

    public Optional<Recipe<?>> getRecipe() {
        if (this.recipe == null && this.recipeId != null && this.recipeObject != null) {
            try {
                this.recipe = RecipeManager.deserialize(this.recipeId, this.recipeObject);
            } catch (Throwable e) {
                this.recipe = null;
            }
        }
        return Optional.ofNullable(this.recipe);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public RecipeInfo with(Identifier recipeId, JsonObject recipeObject) {
        return new RecipeInfo(recipeId, recipeObject);
    }
}