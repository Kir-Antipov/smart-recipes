package dev.kirantipov.smartrecipes.api;

import com.mojang.serialization.Lifecycle;
import dev.kirantipov.smartrecipes.api.event.ServerStateEvents;
import dev.kirantipov.smartrecipes.api.event.WorldStateEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.BiConsumer;

final class RecipeReloadConditions {
    public static final RegistryKey<Registry<RecipeReloadCondition>> KEY = RegistryKey.ofRegistry(new Identifier("recipe_reload_condition"));
    public static final Registry<RecipeReloadCondition> REGISTRY = new SimpleRegistry<>(KEY, Lifecycle.experimental());


    public static final RecipeReloadCondition END_DATA_PACK_RELOAD = create(ServerLifecycleEvents.END_DATA_PACK_RELOAD, ServerLifecycleEvents.SERVER_STARTED, (e, c) -> e.register((s, __, ___) -> c.invoker().onRecipeReloadEvent(s, c.getId())), (e, c) -> e.register(s -> c.invoker().onRecipeReloadEvent(s, c.getId())));

    public static final RecipeReloadCondition PLAYER_JOINED = create(ServerPlayConnectionEvents.JOIN, (e, c) -> e.register((__, ___, s) -> c.invoker().onRecipeReloadEvent(s, c.getId())));

    public static final RecipeReloadCondition PLAYER_DISCONNECTED = create(ServerPlayConnectionEvents.DISCONNECT, (e, c) -> e.register((__, s) -> c.invoker().onRecipeReloadEvent(s, c.getId())));

    public static final RecipeReloadCondition DIFFICULTY_CHANGED = create(ServerStateEvents.DIFFICULTY_CHANGED, (e, c) -> e.register((s, __, ___) -> c.invoker().onRecipeReloadEvent(s, c.getId())));

    public static final RecipeReloadCondition WEATHER_CHANGED = create(WorldStateEvents.WEATHER_CHANGED, (e, c) -> e.register(w -> c.invoker().onRecipeReloadEvent(w.getServer(), c.getId())));

    public static final RecipeReloadCondition TIME_CHANGED = create(WorldStateEvents.TIME_CHANGED, (e, c) -> e.register((w, __, ___) -> c.invoker().onRecipeReloadEvent(w.getServer(), c.getId())));


    private static <T> RecipeReloadCondition create(Event<T> parentEvent, BiConsumer<Event<T>, RecipeReloadCondition> consumer) {
        RecipeReloadCondition reloadCondition = create();
        consumer.accept(parentEvent, reloadCondition);
        return reloadCondition;
    }

    private static <T, E> RecipeReloadCondition create(Event<T> parentEvent, Event<E> secondParentEvent, BiConsumer<Event<T>, RecipeReloadCondition> consumer, BiConsumer<Event<E>, RecipeReloadCondition> secondConsumer) {
        RecipeReloadCondition reloadCondition = create();
        consumer.accept(parentEvent, reloadCondition);
        secondConsumer.accept(secondParentEvent, reloadCondition);
        return reloadCondition;
    }

    private static RecipeReloadCondition create() {
        Event<RecipeReloadCondition.RecipeReloadConditionListener> targetEvent = EventFactory.createArrayBacked(RecipeReloadCondition.RecipeReloadConditionListener.class, callbacks -> (server, id) -> {
            for (RecipeReloadCondition.RecipeReloadConditionListener callback : callbacks) {
                callback.onRecipeReloadEvent(server, id);
            }
        });

        return new RecipeReloadCondition() {
            @Override
            public RecipeReloadConditionListener invoker() {
                return targetEvent.invoker();
            }

            @Override
            public void register(RecipeReloadConditionListener listener) {
                targetEvent.register(listener);
            }
        };
    }
}