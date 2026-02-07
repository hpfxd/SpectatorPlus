package com.hpfxd.spectatorplus.paper.effect;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteArrayDataInput;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

public record SyncedEffect(String effectKey, int amplifier, int duration) {
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeString(buf, effectKey);
        SerializationUtil.writeVarInt(buf, amplifier);
        SerializationUtil.writeVarInt(buf, duration);
    }

    public static SyncedEffect read(ByteArrayDataInput buf) {
        String effectKey = SerializationUtil.readString(buf);
        int amplifier = SerializationUtil.readVarInt(buf);
        int duration = SerializationUtil.readVarInt(buf);
        return new SyncedEffect(effectKey, amplifier, duration);
    }

    public PotionEffectType getType() {
        return PotionEffectType.getByKey(NamespacedKey.fromString(effectKey));
    }
}
