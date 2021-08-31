package dev.kirantipov.smartrecipes;

import dev.kirantipov.smartrecipes.api.networking.SmartRecipesPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmartRecipes implements ClientModInitializer {
    public static final String MOD_ID = "smart-recipes";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier locate(String id) {
        return new Identifier(MOD_ID, id);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        SmartRecipesPackets.initClient();
    }
}