package dev.kir.smartrecipes.util;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class JsonUtil {
    public static boolean isNull(JsonElement jsonElement) {
        return jsonElement == null || jsonElement instanceof JsonNull;
    }

    public static JsonElement get(JsonObject jsonObject, Identifier memberId) {
        JsonElement member = jsonObject.get(memberId.toString());
        return member == null ? jsonObject.get(memberId.getPath()) : member;
    }

    public static Stream<JsonPrimitive> flatMap(JsonElement jsonElement) {
        return flatMap(jsonElement, x -> x, x -> x.entrySet().stream().filter(entry -> asBoolean(entry.getValue())).map(entry -> new JsonPrimitive(entry.getKey())));
    }

    public static <T> Stream<T> flatMap(JsonElement jsonElement, Function<JsonPrimitive, T> primitiveParser, Function<JsonObject, Stream<T>> objectParser) {
        if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            return Stream.of(primitiveParser.apply(jsonPrimitive));
        }

        if (jsonElement instanceof JsonObject jsonObject) {
            return objectParser.apply(jsonObject);
        }

        if (jsonElement instanceof JsonArray jsonArray) {
            return StreamSupport.stream(jsonArray.spliterator(), false).flatMap(e -> flatMap(e, primitiveParser, objectParser));
        }

        return Stream.empty();
    }

    public static boolean asBoolean(JsonElement jsonElement) {
        if (isNull(jsonElement)) {
            return false;
        }

        if (jsonElement instanceof JsonPrimitive primitive) {
            return (
                primitive.isBoolean() && primitive.getAsBoolean() ||
                primitive.isString() && primitive.getAsString().length() != 0 ||
                primitive.isNumber() && primitive.getAsDouble() != 0.0
            );
        }

        if (jsonElement instanceof JsonObject jsonObject) {
            return jsonObject.size() != 0;
        }

        if (jsonElement instanceof JsonArray jsonArray) {
            return jsonArray.size() != 0;
        }

        return true;
    }
}