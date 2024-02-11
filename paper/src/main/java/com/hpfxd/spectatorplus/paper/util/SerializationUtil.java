package com.hpfxd.spectatorplus.paper.util;

import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public final class SerializationUtil {
    private SerializationUtil() {
    }

    public static void writeUuid(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }
}
