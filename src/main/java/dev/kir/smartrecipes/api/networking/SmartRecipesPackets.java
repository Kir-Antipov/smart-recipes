package dev.kir.smartrecipes.api.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SmartRecipesPackets {
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(SynchronizeReloadedRecipesPacket.ID, (client, handler, buf, response) -> {
            SynchronizeReloadedRecipesPacket packet = new SynchronizeReloadedRecipesPacket(buf);
            client.execute(() -> packet.apply(client, handler));
        });
    }
}