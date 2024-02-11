package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public record ClientboundHotbarSyncPacket(
        UUID playerId,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundHotbarSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "hotbar_sync"), ClientboundHotbarSyncPacket::new);

    public static ClientboundHotbarSyncPacket initializing(ServerPlayer target) {
        final ItemStack[] items = new ItemStack[9];

        for (int slot = 0; slot < items.length; slot++) {
            items[slot] = target.getInventory().getItem(slot);
        }

        return new ClientboundHotbarSyncPacket(target.getUUID(), items);
    }

    public ClientboundHotbarSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), readItems(buf));
    }

    private static ItemStack[] readItems(FriendlyByteBuf buf) {
        final int len = buf.readInt();
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            if (buf.readBoolean()) {
                final int length = buf.readInt();
                final ItemStack stack = length > 0 ? readItem(buf, length) : ItemStack.EMPTY;

                items[slot] = stack;
            }
        }

        return items;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt(this.items.length);

        for (final ItemStack item : this.items) {
            buf.writeBoolean(item != null);

            if (item != null) {
                if (item.isEmpty()) {
                    buf.writeInt(0);
                } else {
                    final byte[] itemData = writeItem(item);
                    buf.writeInt(itemData.length);
                    buf.writeBytes(itemData);
                }
            }
        }
    }

    private static ItemStack readItem(FriendlyByteBuf buf, int len) {
        try {
            final byte[] in = new byte[len];
            buf.readBytes(in);

            final CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(in), NbtAccounter.unlimitedHeap());
            return ItemStack.of(tag);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    private static byte[] writeItem(ItemStack item) {
        try {
            final CompoundTag tag = new CompoundTag();
            item.save(tag);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, out);

            return out.toByteArray();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
