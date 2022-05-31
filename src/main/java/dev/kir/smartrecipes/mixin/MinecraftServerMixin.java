package dev.kir.smartrecipes.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import dev.kir.smartrecipes.api.event.ServerStateEvents;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
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

    @Unique
    private GameMode gameMode;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, @Nullable MinecraftSessionService sessionService, @Nullable GameProfileRepository gameProfileRepo, @Nullable UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.difficulty = saveProperties.getDifficulty();
        this.gameMode = saveProperties.getGameMode();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer)(Object)this;

        if (this.difficulty != this.saveProperties.getDifficulty()) {
            ServerStateEvents.DIFFICULTY_CHANGED.invoker().onDifficultyChanged(server, this.difficulty, this.saveProperties.getDifficulty());
            this.difficulty = this.saveProperties.getDifficulty();
        }

        if (this.gameMode != this.saveProperties.getGameMode()) {
            ServerStateEvents.GAMEMODE_CHANGED.invoker().onGameModeChanged(server, this.gameMode, this.saveProperties.getGameMode());
            this.gameMode = this.saveProperties.getGameMode();
        }
    }
}