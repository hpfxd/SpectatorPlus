package com.hpfxd.spectatorplus.fabric.sync;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface ClientboundSyncPacket extends CustomPacketPayload {
    UUID playerId();

    boolean canSend(ServerPlayer receiver);
}
