package com.hpfxd.spectatorplus.fabric.mixin;

import com.google.common.collect.Lists;
import com.hpfxd.spectatorplus.fabric.SpectatorMod;
import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
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

            // Send initial map data patch packet if the target has a map in inventory
            for (final ItemStack stack : target.getInventory().items) {
                if (stack.is(Items.FILLED_MAP)) {
                    final MapId mapId = stack.get(DataComponents.MAP_ID);
                    final MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.level());

                    if (mapItemSavedData != null) {
                        spectator.connection.send(getInitialMapDataPacket(mapId, mapItemSavedData));
                    }
                }
            }
        }
    }

    /**
     * Constructs a new {@link ClientboundMapItemDataPacket} containing all data from {@link MapItemSavedData} and not
     * relying that the player has previously received any updates of this map.
     */
    @Unique
    private static ClientboundMapItemDataPacket getInitialMapDataPacket(MapId mapId, MapItemSavedData data) {
        return new ClientboundMapItemDataPacket(mapId, data.scale, data.locked, Lists.newArrayList(data.getDecorations()), new MapItemSavedData.MapPatch(0, 0, 128, 128, data.colors));
    }

    @Inject(
            method = "doTick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ComplexItem;getUpdatePacket(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/network/protocol/Packet;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getHealth()F", ordinal = 0)
            )
    )
    private void spectatorplus$syncMapData(CallbackInfo ci, @Local Packet<?> packet) {
        // Send map packet to any spectators of this player. If this is only an update patch of a previously sent map,
        // any spectators would have already received previous updates, so sending this is fine.
        for (final ServerPlayer spectator : ServerSyncController.getSpectators(this)) {
            spectator.connection.send(packet);
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
