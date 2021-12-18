package dev.kir.smartrecipes.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.JsonHelper;

import java.util.stream.Stream;

final record ModEntry(String id, String version) {
    public ModEntry(String id) {
        this(id, "*");
    }

    public static ModEntry parse(JsonPrimitive jsonPrimitive) {
        return new ModEntry(jsonPrimitive.getAsString());
    }

    public static Stream<ModEntry> parse(JsonObject jsonObject) {
        String id = JsonHelper.getString(jsonObject, "id");
        String version = jsonObject.get("version") instanceof JsonPrimitive versionPrimitive ? versionPrimitive.getAsString() : null;
        return Stream.of(version == null ? new ModEntry(id) : new ModEntry(id, version));
    }
}