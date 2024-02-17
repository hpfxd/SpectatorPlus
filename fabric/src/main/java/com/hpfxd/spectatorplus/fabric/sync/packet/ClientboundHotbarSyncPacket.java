package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import io.netty.handler.codec.EncoderException;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
    public static final int ITEMS_LENGTH = 9;
    public static final PacketType<ClientboundHotbarSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "hotbar_sync"), ClientboundHotbarSyncPacket::new);
    static final String PERMISSION = "spectatorplus.sync.hotbar";

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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        writeItems(buf, this.items);
    }

    public static ItemStack[] readItems(FriendlyByteBuf buf) {
        final int len = buf.readInt();
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            if (buf.readBoolean()) {
                final int length = buf.readInt();
                final ItemStack stack = readItem(buf, length);

                items[slot] = stack;
            }
        }

        return items;
    }

    public static void writeItems(FriendlyByteBuf buf, ItemStack[] items) {
        buf.writeInt(items.length);

        for (final ItemStack item : items) {
            buf.writeBoolean(item != null);

            if (item != null) {
                final byte[] itemData = writeItem(item);
                buf.writeInt(itemData.length);
                buf.writeBytes(itemData);
            }
        }
    }

    public static ItemStack readItem(FriendlyByteBuf buf, int len) {
        if (len == 0) {
            return ItemStack.EMPTY;
        }

        try {
            final byte[] in = new byte[len];
            buf.readBytes(in);

            final CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(in), NbtAccounter.unlimitedHeap());
            return ItemStack.of(tag);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public static byte[] writeItem(ItemStack item) {
        if (item.isEmpty()) {
            return new byte[0];
        }

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

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return Permissions.check(receiver, PERMISSION, true);
    }
}
