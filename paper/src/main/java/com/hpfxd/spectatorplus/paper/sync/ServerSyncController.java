package com.hpfxd.spectatorplus.paper.sync;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.handler.ExperienceSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.FoodSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.HotbarSyncHandler;
import com.hpfxd.spectatorplus.paper.sync.handler.SelectedSlotSyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ServerSyncController implements PluginMessageListener {
    private final SpectatorPlugin plugin;

    public ServerSyncController(SpectatorPlugin plugin) {
        this.plugin = plugin;

        for (final NamespacedKey key : SyncPackets.CLIENTBOUND.keySet()) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, key.asString());
        }

        for (final NamespacedKey key : SyncPackets.SERVERBOUND.keySet()) {
            Bukkit.getMessenger().registerIncomingPluginChannel(plugin, key.asString(), this);
        }

        this.plugin.getSLF4JLogger().info("Registered {} clientbound, {} serverbound sync packets.", SyncPackets.CLIENTBOUND.size(), SyncPackets.SERVERBOUND.size());

        new ExperienceSyncHandler(plugin);
        new FoodSyncHandler(plugin);
        new HotbarSyncHandler(plugin);
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

    public Iterable<Player> getSpectators(Player target, String permission) {
        return Iterables.filter(target.getWorld().getPlayers(), p -> target.equals(p.getSpectatorTarget()) && p.hasPermission(permission));
    }

    public void broadcastPacketToSpectators(Player target, String permission, ClientboundSyncPacket packet) {
        this.sendPacket(this.getSpectators(target, permission), packet);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
    }
}
