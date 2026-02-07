package com.hpfxd.spectatorplus.fabric.sync;

import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class SyncedEffect {
    public final String effectKey;
    public final int amplifier;
    public final int duration;

    public SyncedEffect(String effectKey, int amplifier, int duration) {
        this.effectKey = effectKey;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public static SyncedEffect read(FriendlyByteBuf buf) {
        String effectKey = buf.readUtf();
        int amplifier = buf.readVarInt();
        int duration = buf.readVarInt();
        return new SyncedEffect(effectKey, amplifier, duration);
    }

    public static List<SyncedEffect> readList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<SyncedEffect> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(SyncedEffect.read(buf));
        }
        return list;
    }
}
