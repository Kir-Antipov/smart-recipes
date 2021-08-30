package dev.kirantipov.smartrecipes.mixin;

import dev.kirantipov.smartrecipes.api.event.WorldStateEvents;
import dev.kirantipov.smartrecipes.util.world.TimeOfDay;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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

    private ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, CallbackInfo ci) {
        this.wasRaining = properties.isRaining();
        this.wasThundering = properties.isThundering();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (this.wasRaining != this.worldProperties.isRaining() || this.wasThundering != this.worldProperties.isThundering()) {
            WorldStateEvents.WEATHER_CHANGED.invoker().onWeatherChanged((ServerWorld)(Object)this);
            this.wasRaining = this.worldProperties.isRaining();
            this.wasThundering = this.worldProperties.isThundering();
        }
    }
}