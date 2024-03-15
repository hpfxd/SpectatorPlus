package com.hpfxd.spectatorplus.fabric.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public final class CustomPacketCodecs {
    private CustomPacketCodecs() {
    }

    public static ItemStack[] readItems(RegistryFriendlyByteBuf buf) {
        final int len = buf.readInt();
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            if (buf.readBoolean()) {
                final ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

                items[slot] = stack;
            }
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
}
