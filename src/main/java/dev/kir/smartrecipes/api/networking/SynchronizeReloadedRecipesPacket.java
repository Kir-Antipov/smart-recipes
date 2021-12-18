package dev.kir.smartrecipes.api.networking;

import com.google.gson.JsonObject;
import dev.kir.smartrecipes.SmartRecipes;
import dev.kir.smartrecipes.api.ReloadableRecipeManager;
import dev.kir.smartrecipes.util.recipe.RecipeBookUtil;
import dev.kir.smartrecipes.api.RecipeInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class SynchronizeReloadedRecipesPacket {
    public static final Identifier ID = SmartRecipes.locate("packet.sync.recipes");

    private final Collection<Pair<ReloadableRecipeManager.RecipeState, RecipeInfo>> reloadedRecipes;

    public SynchronizeReloadedRecipesPacket(Collection<Pair<ReloadableRecipeManager.RecipeState, RecipeInfo>> reloadedRecipes) {
        this.reloadedRecipes = reloadedRecipes;
    }

    public SynchronizeReloadedRecipesPacket(PacketByteBuf buf) {
        this.reloadedRecipes = buf.readList(SynchronizeReloadedRecipesPacket::readRecipeEntry);
    }

    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.reloadedRecipes, SynchronizeReloadedRecipesPacket::writeRecipeEntry);
    }

    @Environment(EnvType.CLIENT)
    public void apply(MinecraftClient client, ClientPlayNetworkHandler handler) {
        RecipeManager recipeManager = handler.getRecipeManager();
        ((ReloadableRecipeManager)recipeManager).apply(this.reloadedRecipes);
        RecipeBook recipeBook = client.player == null ? null : client.player.getRecipeBook();
        if (recipeBook != null) {
            RecipeBookUtil.apply(recipeBook, this.reloadedRecipes);
        }
    }

    public void send(Stream<ServerPlayerEntity> players) {
        PacketByteBuf buffer = PacketByteBufs.create();
        this.write(buffer);
        players.forEach(player -> ServerPlayNetworking.send(player, ID, buffer));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> void writeRecipeEntry(PacketByteBuf buf, Pair<ReloadableRecipeManager.RecipeState, RecipeInfo> recipeEntry) {
        ReloadableRecipeManager.RecipeState state = recipeEntry.getLeft();
        RecipeInfo recipeInfo = recipeEntry.getRight();
        if (state == ReloadableRecipeManager.RecipeState.KEEP) {
            T recipe = (T)recipeInfo.getRecipe().orElseThrow(() -> new IllegalArgumentException("Unable to parse recipe '" + recipeInfo.getRecipeId() + "'"));

            buf.writeBoolean(true);
            buf.writeIdentifier(Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer()));
            buf.writeIdentifier(recipe.getId());
            ((RecipeSerializer<T>)recipe.getSerializer()).write(buf, recipe);
        } else {
            buf.writeBoolean(false);
            buf.writeIdentifier(recipeInfo.getRecipeId());
            buf.writeIdentifier(Registry.RECIPE_TYPE.getId(recipeInfo.getRecipeType().orElseThrow(() -> new IllegalArgumentException("Recipe '" + recipeInfo.getRecipeId() + "' uses invalid or unsupported recipe type"))));
        }
    }

    private static Pair<ReloadableRecipeManager.RecipeState, RecipeInfo> readRecipeEntry(PacketByteBuf buf) {
        if (buf.readBoolean()) {
            Identifier serializerId = buf.readIdentifier();
            RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.getOrEmpty(serializerId).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + serializerId));
            Identifier recipeId = buf.readIdentifier();
            Recipe<?> recipe = serializer.read(recipeId, buf);
            return new Pair<>(ReloadableRecipeManager.RecipeState.KEEP, new SerializableRecipeInfo(recipeId, recipe));
        } else {
            Identifier recipeId = buf.readIdentifier();
            Identifier recipeTypeId = buf.readIdentifier();
            RecipeType<?> recipeType = Registry.RECIPE_TYPE.getOrEmpty(recipeTypeId).orElseThrow(() -> new IllegalArgumentException("Invalid or unsupported recipe type '" + recipeTypeId + "'"));
            return new Pair<>(ReloadableRecipeManager.RecipeState.REMOVE, new SerializableRecipeInfo(recipeId, recipeType));
        }
    }

    private static class SerializableRecipeInfo extends RecipeInfo {
        private final Recipe<?> recipe;
        private final RecipeType<?> recipeType;

        public SerializableRecipeInfo(Identifier recipeId, RecipeType<?> recipeType) {
            super(recipeId, null);
            this.recipe = null;
            this.recipeType = recipeType;
        }

        public SerializableRecipeInfo(Identifier recipeId, Recipe<?> recipe) {
            super(recipeId, null);
            this.recipe = recipe;
            this.recipeType = null;
        }

        @Override
        public Optional<Recipe<?>> getRecipe() {
            return Optional.ofNullable(this.recipe);
        }

        @Override
        public Optional<RecipeType<?>> getRecipeType() {
            return this.recipeType == null ? this.getRecipe().map(Recipe::getType) : Optional.of(this.recipeType);
        }

        @Override
        public JsonObject getRecipeAsJson() {
            throw new IllegalStateException("JSON is not available on the client side");
        }
    }
}