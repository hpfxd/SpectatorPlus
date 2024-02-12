package com.hpfxd.spectatorplus.fabric.sync;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface ClientboundSyncPacket extends FabricPacket {
    UUID playerId();

    boolean canSend(ServerPlayer receiver);
}
