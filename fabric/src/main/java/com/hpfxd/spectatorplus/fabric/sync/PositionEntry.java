package com.hpfxd.spectatorplus.fabric.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public record PositionEntry(
        float partialTicks,
        float yaw,
        float pitch
) {
    public static PositionEntry interpolate(float partialTicks, PositionEntry lower, PositionEntry higher) {
        final float delta = Mth.map(partialTicks, lower.partialTicks, higher.partialTicks, 0f, 1f);

        return new PositionEntry(
                partialTicks,
                Mth.lerp(delta, lower.yaw, higher.yaw),
                Mth.lerp(delta, lower.pitch, higher.pitch)
        );
    }

    public static PositionEntry[] readEntries(FriendlyByteBuf buf) {
        final int len = buf.readVarInt();
        final PositionEntry[] entries = new PositionEntry[len];

        for (int i = 0; i < len; i++) {
            entries[i] = new PositionEntry(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        return entries;
    }

    public static void writeEntries(FriendlyByteBuf buf, PositionEntry[] entries) {
        buf.writeVarInt(entries.length);

        // todo: transmit only deltas between entries
        for (final PositionEntry entry : entries) {
            buf.writeFloat(entry.partialTicks());
            buf.writeFloat(entry.yaw());
            buf.writeFloat(entry.pitch());
        }
    }
}
