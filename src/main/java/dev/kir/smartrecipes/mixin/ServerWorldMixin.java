package dev.kir.smartrecipes.mixin;

import dev.kir.smartrecipes.api.event.WorldStateEvents;
import dev.kir.smartrecipes.util.world.TimeOfDay;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin extends World {
    @Final
    @Shadow
    private ServerWorldProperties worldProperties;

    @Unique
    private boolean wasRaining;

    @Unique
    private boolean wasThundering;

    @Unique
    private TimeOfDay timeOfDay;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<?> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<?> spawners, boolean shouldTickTime, CallbackInfo ci) {
        this.wasRaining = properties.isRaining();
        this.wasThundering = properties.isThundering();
        this.timeOfDay = TimeOfDay.fromTime(properties.getTimeOfDay());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (this.wasRaining != this.worldProperties.isRaining() || this.wasThundering != this.worldProperties.isThundering()) {
            WorldStateEvents.WEATHER_CHANGED.invoker().onWeatherChanged((ServerWorld)(Object)this);
            this.wasRaining = this.worldProperties.isRaining();
            this.wasThundering = this.worldProperties.isThundering();
        }
    }

    @Inject(method = "setTimeOfDay", at = @At("RETURN"))
    private void setTimeOfDay(long time, CallbackInfo ci) {
        TimeOfDay newTimeOfDay = TimeOfDay.fromTime(time);
        if (this.timeOfDay != newTimeOfDay) {
            WorldStateEvents.TIME_CHANGED.invoker().onTimeOfDayChanged((ServerWorld)(Object)this, this.timeOfDay, newTimeOfDay);
            this.timeOfDay = newTimeOfDay;
        }
    }
}