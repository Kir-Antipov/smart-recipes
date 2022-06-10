package dev.kir.smartrecipes.util.recipe;

import dev.kir.smartrecipes.api.RecipeInfo;
import dev.kir.smartrecipes.api.ReloadableRecipeManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Collection;

public final class RecipeBookUtil {
    public static void apply(RecipeBook recipeBook, Collection<Pair<ReloadableRecipeManager.RecipeState, RecipeInfo>> diff) {
        apply(recipeBook, diff, false);
    }

    public static void apply(RecipeBook recipeBook, Collection<Pair<ReloadableRecipeManager.RecipeState, RecipeInfo>> diff, boolean showRecipeToasts) {
        if (diff.isEmpty()) {
            return;
        }

        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        for (Pair<ReloadableRecipeManager.RecipeState, RecipeInfo> entry : diff) {
            ReloadableRecipeManager.RecipeState recipeState = entry.getLeft();
            RecipeInfo recipeInfo = entry.getRight();
            Identifier recipeId = recipeInfo.getRecipeId();
            if (recipeState == ReloadableRecipeManager.RecipeState.KEEP && recipeInfo.getRecipe().isPresent()) {
                Recipe<?> recipe = recipeInfo.getRecipe().get();
                if (showRecipeToasts && isClient && !(recipeBook instanceof ServerRecipeBook) && !recipeBook.contains(recipeId)) {
                    recipeBook.display(recipe);
                    showRecipeToast(recipe);
                }
                recipeBook.add(recipe);
            } else {
                recipeBook.remove(recipeId);
            }
        }

        if (isClient) {
            refreshRecipeBook(recipeBook);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void refreshRecipeBook(RecipeBook recipeBook) {
        if (!(recipeBook instanceof ClientRecipeBook clientRecipeBook)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler network = MinecraftClient.getInstance().getNetworkHandler();
        if (network != null) {
            clientRecipeBook.reload(network.getRecipeManager().values());
            for (RecipeResultCollection collection : clientRecipeBook.getOrderedResults()) {
                collection.initialize(clientRecipeBook);
            }
            client.reloadSearchProvider(SearchManager.RECIPE_OUTPUT, clientRecipeBook.getOrderedResults());
        }

        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider recipeBookProvider) {
            recipeBookProvider.refreshRecipeBook();
        }
    }

    @Environment(EnvType.CLIENT)
    private static void showRecipeToast(Recipe<?> recipe) {
        RecipeToast.show(MinecraftClient.getInstance().getToastManager(), recipe);
    }
}