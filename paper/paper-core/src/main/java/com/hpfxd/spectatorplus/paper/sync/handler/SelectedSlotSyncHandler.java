package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundSelectedSlotSyncPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import static com.hpfxd.spectatorplus.paper.sync.handler.InventorySyncHandler.HOTBAR_PERMISSION;

public class SelectedSlotSyncHandler implements Listener {
    private final SpectatorPlugin plugin;

    public SelectedSlotSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(HOTBAR_PERMISSION)) {
            this.plugin.getSyncController().sendPacket(spectator, new ClientboundSelectedSlotSyncPacket(target.getUniqueId(), target.getInventory().getHeldItemSlot()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangeHeldItem(PlayerItemHeldEvent event) {
        final Player target = event.getPlayer();
        this.plugin.getSyncController().broadcastPacketToSpectators(target, HOTBAR_PERMISSION, new ClientboundSelectedSlotSyncPacket(target.getUniqueId(), event.getNewSlot()));
    }
}
