package com.hpfxd.spectatorplus.fabric.sync;

import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class CustomPacketCodecs {
    private CustomPacketCodecs() {
    }

    public static ItemStack[] readItems(RegistryFriendlyByteBuf buf) {
        final int len = buf.readInt();
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            if (buf.readBoolean()) {
                final ItemStack stack = readItem(buf);

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
                writeItem(buf, item);
            }
        }
    }

    public static ItemStack readItem(RegistryFriendlyByteBuf buf) {
        final int len = buf.readInt();
        if (len == 0) {
            return ItemStack.EMPTY;
        }

        try {
            final byte[] in = new byte[len];
            buf.readBytes(in);

            final CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(in), NbtAccounter.unlimitedHeap());
            return ItemStack.parse(buf.registryAccess(), tag).orElse(ItemStack.EMPTY);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public static void writeItem(RegistryFriendlyByteBuf buf, ItemStack item) {
        if (item.isEmpty()) {
            buf.writeInt(0);
            return;
        }

        final byte[] bytes;
        try {
            final CompoundTag tag = new CompoundTag();
            item.save(buf.registryAccess(), tag);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, out);

            bytes = out.toByteArray();
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
