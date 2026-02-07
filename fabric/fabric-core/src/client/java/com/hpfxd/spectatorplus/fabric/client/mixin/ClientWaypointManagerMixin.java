package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ClientWaypointManager.class)
public class ClientWaypointManagerMixin {
    @Shadow
    @Final
    private Map<Either<UUID, String>, TrackedWaypoint> waypoints;

    @Unique
    private boolean shouldIgnore(TrackedWaypoint waypoint) {
        var client = Minecraft.getInstance();
        return waypoint.distanceSquared(client.player) < 1.0;
    }

    @Inject(method = "hasWaypoints", at = @At("HEAD"), cancellable = true)
    private void spectatorplus$hasWaypoints(CallbackInfoReturnable<Boolean> cir) {
        if (ClientSyncController.syncData != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "forEachWaypoint", at = @At("HEAD"), cancellable = true)
    private void spectatorplus$forEachWaypoint(Entity entity, Consumer<TrackedWaypoint> action, CallbackInfo ci) {
        if (ClientSyncController.syncData != null) {
            ci.cancel();
        }
    }
}