package dev.kir.smartrecipes.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public class RecipeInfo {
    private final Identifier recipeId;
    private final JsonObject recipeObject;
    private RecipeEntry<?> recipeEntry;
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
        if (this.recipeEntry != null) {
            return Optional.of(this.recipeEntry.value().getType());
        }

        if (this.recipeType == null && this.recipeObject != null) {
            String type = this.recipeObject.get("type") instanceof JsonPrimitive typePrimitive && typePrimitive.isString() ? typePrimitive.getAsString() : null;
            Identifier id = type == null ? null : Identifier.tryParse(type);
            if (id != null) {
                this.recipeType = Registries.RECIPE_TYPE.getOrEmpty(id).or(() -> Registries.RECIPE_TYPE.getOrEmpty(new Identifier(id.getNamespace(), id.getPath().split("_")[0]))).orElse(null);
            }
        }
        return Optional.ofNullable(this.recipeType);
    }

    public Optional<RecipeEntry<?>> getRecipeEntry() {
        if (this.recipeEntry == null && this.recipeId != null && this.recipeObject != null) {
            try {
                this.recipeEntry = RecipeManager.deserialize(this.recipeId, this.recipeObject);
            } catch (Throwable e) {
                this.recipeEntry = null;
            }
        }
        return Optional.ofNullable(this.recipeEntry);
    }

    public Optional<Recipe<?>> getRecipe() {
        return this.getRecipeEntry().map(RecipeEntry::value);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public RecipeInfo with(Identifier recipeId, JsonObject recipeObject) {
        return new RecipeInfo(recipeId, recipeObject);
    }
}