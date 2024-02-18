package com.hpfxd.spectatorplus.paper.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public final class SerializationUtil {
    private SerializationUtil() {
    }

    public static UUID readUuid(ByteArrayDataInput buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUuid(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeVarInt(ByteArrayDataOutput out, int value) {
        while((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        out.writeByte(value);
    }
}
