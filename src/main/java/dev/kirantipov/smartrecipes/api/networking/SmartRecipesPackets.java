package dev.kirantipov.smartrecipes.api.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SmartRecipesPackets {
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(SynchronizeReloadedRecipesPacket.ID, (client, handler, buf, response) -> new SynchronizeReloadedRecipesPacket(buf).apply(client, handler));
    }
}