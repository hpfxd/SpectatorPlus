package com.hpfxd.spectatorplus.fabric.sync;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CustomPacketCodecs {
    private CustomPacketCodecs() {
    }

    public static ItemStack[] readItems(RegistryFriendlyByteBuf buf) {
        final int len = buf.readInt();
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            items[slot] = buf.readBoolean() ? ItemStack.OPTIONAL_STREAM_CODEC.decode(buf) : null;
        }

        return items;
    }

    public static void writeItems(RegistryFriendlyByteBuf buf, ItemStack[] items) {
        buf.writeInt(items.length);

        for (final ItemStack item : items) {
            buf.writeBoolean(item != null);
            if (item != null) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
            }
        }
    }

    public static ItemStack readItem(RegistryFriendlyByteBuf buf) {
        try {
            return ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        } catch (Exception e) {
            throw new DecoderException("Failed to read ItemStack", e);
        }
    }


    public static void writeItem(RegistryFriendlyByteBuf buf, @NotNull ItemStack item) {
        try {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
        } catch (Exception e) {
            throw new EncoderException("Failed to write ItemStack", e);
        }
    }
}