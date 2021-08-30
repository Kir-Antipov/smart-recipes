package dev.kirantipov.smartrecipes.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import dev.kirantipov.smartrecipes.api.event.ServerStateEvents;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    @Final
    @Shadow
    protected SaveProperties saveProperties;

    @Unique
    private Difficulty difficulty;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Thread serverThread, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager dataPackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.difficulty = saveProperties.getDifficulty();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer)(Object)this;

        if (this.difficulty != this.saveProperties.getDifficulty()) {
            ServerStateEvents.DIFFICULTY_CHANGED.invoker().onDifficultyChanged(server, this.difficulty, this.saveProperties.getDifficulty());
            this.difficulty = this.saveProperties.getDifficulty();
        }
    }
}