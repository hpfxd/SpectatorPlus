package com.hpfxd.spectatorplus.paper.sync;

import com.google.common.io.ByteArrayDataOutput;

public interface ClientboundSyncPacket extends SyncPacket {
    void write(ByteArrayDataOutput buf);
}
