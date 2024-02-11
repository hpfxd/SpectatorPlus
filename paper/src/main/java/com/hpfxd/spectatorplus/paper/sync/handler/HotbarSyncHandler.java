package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundHotbarSyncPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarSyncHandler implements Listener {
    private final SpectatorPlugin plugin;
    private final Map<UUID, ItemStack[]> playerHotbars = new HashMap<>();

    public HotbarSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 0);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void tick() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final ItemStack[] slots = this.playerHotbars.computeIfAbsent(player.getUniqueId(), k -> {
                final ItemStack[] arr = new ItemStack[9];
                Arrays.fill(arr, ItemStack.empty());
                return arr;
            });

            final ItemStack[] sendSlots = new ItemStack[slots.length];
            boolean updated = false;

            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null) {
                    item = ItemStack.empty();
                }

                if (!item.equals(slots[i])) {
                    slots[i] = item.clone();
                    sendSlots[i] = item;
                    updated = true;
                }
            }

            if (updated) {
                this.plugin.getSyncController().broadcastPacketToSpectators(player, new ClientboundHotbarSyncPacket(player.getUniqueId(), sendSlots));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        if (event.getNewSpectatorTarget() instanceof final Player target) {
            final ItemStack[] slots = new ItemStack[9];
            for (int slot = 0; slot < slots.length; slot++) {
                ItemStack item = target.getInventory().getItem(slot);
                if (item == null) {
                    item = ItemStack.empty();
                }

                slots[slot] = item;
            }

            this.plugin.getSyncController().sendPacket(spectator, new ClientboundHotbarSyncPacket(target.getUniqueId(), slots));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerHotbars.remove(event.getPlayer().getUniqueId());
    }
}
