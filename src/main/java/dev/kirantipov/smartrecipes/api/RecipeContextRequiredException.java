package dev.kirantipov.smartrecipes.api;

public class RecipeContextRequiredException extends IllegalArgumentException {
    public RecipeContextRequiredException() { }

    public RecipeContextRequiredException(String message) {
        super(message);
    }
}