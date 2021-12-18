package dev.kir.smartrecipes.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.JsonHelper;

import java.util.stream.Stream;

final record RegistryEntry(String registry, String entry) {
    public RegistryEntry(String entry) {
        this("block", entry);
    }

    public static RegistryEntry parse(JsonPrimitive jsonPrimitive) {
        return new RegistryEntry(jsonPrimitive.getAsString());
    }

    public static Stream<RegistryEntry> parse(JsonObject jsonObject) {
        String registry = jsonObject.get("registry") instanceof JsonPrimitive registryPrimitive ? registryPrimitive.getAsString() : null;
        String entry = JsonHelper.getString(jsonObject, "entry");
        return Stream.of(registry == null ? new RegistryEntry(entry) : new RegistryEntry(registry, entry));
    }
}