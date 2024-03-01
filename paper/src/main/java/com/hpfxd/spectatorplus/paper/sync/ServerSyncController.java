package com.hpfxd.spectatorplus.paper.sync;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.handler.ExperienceSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.FoodSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.InventorySyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.SelectedSlotSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.screen.ScreenSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ServerboundRequestInventoryOpenPacket;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServerSyncController implements PluginMessageListener {
    private final SpectatorPlugin plugin;
    private final ScreenSyncHandler screenSyncHandler;
    private final InventorySyncHandler inventorySyncHandler;

    public ServerSyncController(SpectatorPlugin plugin) {
        this.plugin = plugin;

        for (final NamespacedKey key : SyncPackets.CLIENTBOUND.keySet()) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, key.asString());
        }

        for (final NamespacedKey key : SyncPackets.SERVERBOUND.keySet()) {
            Bukkit.getMessenger().registerIncomingPluginChannel(plugin, key.asString(), this);
        }

        this.plugin.getSLF4JLogger().info("Registered {} clientbound, {} serverbound sync packets.", SyncPackets.CLIENTBOUND.size(), SyncPackets.SERVERBOUND.size());

        this.screenSyncHandler = new ScreenSyncHandler(plugin);
        new ExperienceSyncHandler(plugin);
        new FoodSyncHandler(plugin);
        this.inventorySyncHandler = new InventorySyncHandler(plugin);
        new SelectedSlotSyncHandler(plugin);
    }

    public void sendPacket(Player receiver, ClientboundSyncPacket packet) {
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        packet.write(buf);

        receiver.sendPluginMessage(this.plugin, packet.channel().asString(), buf.toByteArray());
    }

    public void sendPacket(Iterable<Player> receivers, ClientboundSyncPacket packet) {
        final Iterator<Player> it = receivers.iterator();
        if (!it.hasNext()) return;

        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        packet.write(buf);

        it.forEachRemaining(receiver -> receiver.sendPluginMessage(this.plugin, packet.channel().asString(), buf.toByteArray()));
    }

    public Iterable<Player> getSpectators(Player target, Predicate<Player> predicate) {
        return Iterables.filter(target.getWorld().getPlayers(), p -> target.equals(p.getSpectatorTarget()) && predicate.test(p));
    }

    public Iterable<Player> getSpectators(Player target, String permission) {
        return this.getSpectators(target, spectator -> spectator.hasPermission(permission));
    }

    public void broadcastPacketToSpectators(Player target, String permission, ClientboundSyncPacket packet) {
        this.sendPacket(this.getSpectators(target, permission), packet);
    }

    public ScreenSyncHandler getScreenSyncHandler() {
        return this.screenSyncHandler;
    }

    public InventorySyncHandler getInventorySyncHandler() {
        return this.inventorySyncHandler;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        final NamespacedKey key = NamespacedKey.fromString(channel);
        final Function<ByteArrayDataInput, ? extends ServerboundSyncPacket> constructor = SyncPackets.SERVERBOUND.get(key);

        if (constructor == null) {
            this.plugin.getSLF4JLogger().warn("Received unknown packet on channel \"{}\"", channel);
            return;
        }

        final ByteArrayDataInput buf = ByteStreams.newDataInput(message);
        final ServerboundSyncPacket packet = constructor.apply(buf);

        if (packet instanceof ServerboundOpenedInventorySyncPacket p) {
            this.handle(player, p);
        } if (packet instanceof ServerboundRequestInventoryOpenPacket p) {
            this.handle(player, p);
        }
    }

    private void handle(Player sender, ServerboundOpenedInventorySyncPacket packet) {
        this.screenSyncHandler.onPlayerOpenInventory(sender);
    }

    private void handle(Player sender, ServerboundRequestInventoryOpenPacket packet) {
        final Player target = Bukkit.getPlayer(packet.playerId());

        if (target != null) {
            this.screenSyncHandler.onRequestOpen(sender, target);
        }
    }
}
