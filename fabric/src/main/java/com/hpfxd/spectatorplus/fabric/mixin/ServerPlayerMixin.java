package com.hpfxd.spectatorplus.fabric.mixin;

import com.hpfxd.spectatorplus.fabric.SpectatorMod;
import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow public abstract void teleportTo(ServerLevel newLevel, double x, double y, double z, float yaw, float pitch);
    @Shadow public abstract void setCamera(@Nullable Entity entityToSpectate);

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "doTick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastFoodSaturationZero:Z", opcode = Opcodes.PUTFIELD))
    private void spectatorplus$syncFood(CallbackInfo ci) {
        ServerSyncController.broadcastPacketToSpectators((ServerPlayer) (Object) this, new ClientboundFoodSyncPacket(this.getUUID(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
    }

    @Inject(method = "doTick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentExp:I", opcode = Opcodes.PUTFIELD))
    private void spectatorplus$syncExperience(CallbackInfo ci) {
        ServerSyncController.broadcastPacketToSpectators((ServerPlayer) (Object) this, new ClientboundExperienceSyncPacket(this.getUUID(), this.experienceProgress, this.getXpNeededForNextLevel(), this.experienceLevel));
    }

    @Inject(method = "setCamera(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void spectatorplus$syncToNewSpectator(Entity entityToSpectate, CallbackInfo ci) {
        if (entityToSpectate instanceof final ServerPlayer target) {
            final ServerPlayer spectator = (ServerPlayer) (Object) this;

            ServerSyncController.sendPacket(spectator, ClientboundExperienceSyncPacket.initializing(target));
            ServerSyncController.sendPacket(spectator, ClientboundFoodSyncPacket.initializing(target));
            ServerSyncController.sendPacket(spectator, ClientboundHotbarSyncPacket.initializing(target));
            ServerSyncController.sendPacket(spectator, ClientboundSelectedSlotSyncPacket.initializing(target));
        }
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;move(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void spectatorplus$allowTransferBetweenLevels(CallbackInfo ci, @Local(ordinal = 0) Entity entity) {
        // MC-261799
        // Adapted fix from https://github.com/PaperMC/Paper/pull/9349

        if (entity.level() == this.level()) {
            if (SpectatorMod.config.autoUpdatePosition && this.tickCount % 20 == 0) {
                // We send the player an additional teleport packet here to indicate that the position of itself has been moved.
                // Without this packet, if a player travels a too far distance, chunks will start to become invisible for our spectator.

                this.connection.teleport(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            }
        } else if (SpectatorMod.config.allowTransferBetweenLevels) {
            // Teleport ourselves to our camera
            this.teleportTo((ServerLevel) entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());

            // Update the tracker of the other dimension for our cross-dimension teleport
            final var entityMap = ((ChunkMapAccessor) ((ServerLevel) entity.level()).getChunkSource().chunkMap).getEntityMap();
            final ChunkMap.TrackedEntity tracker = entityMap.get(entity.getId());
            if (tracker != null) {
                tracker.updatePlayer((ServerPlayer) (Object) this);
            }

            this.setCamera(entity);
        }
    }
}
