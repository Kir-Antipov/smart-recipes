package dev.kir.smartrecipes.api;

public class RecipeContextRequiredException extends IllegalArgumentException {
    public RecipeContextRequiredException() { }

    public RecipeContextRequiredException(String message) {
        super(message);
    }
}