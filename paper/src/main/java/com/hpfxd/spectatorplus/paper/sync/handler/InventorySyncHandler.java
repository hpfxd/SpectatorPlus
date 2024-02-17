package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundInventorySyncPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventorySyncHandler implements Listener {
    public static final String HOTBAR_PERMISSION = "spectatorplus.sync.hotbar";
    public static final String INVENTORY_PERMISSION = "spectatorplus.sync.inventory";

    private final SpectatorPlugin plugin;
    private final Map<UUID, ItemStack[]> playerInventories = new HashMap<>();

    public InventorySyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 0);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void tick() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final ItemStack[] slots = this.playerInventories.computeIfAbsent(player.getUniqueId(), k -> {
                final ItemStack[] arr = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
                Arrays.fill(arr, ItemStack.empty());
                return arr;
            });

            final ItemStack[] inventorySendSlots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
            final ItemStack[] hotbarSendSlots = new ItemStack[ClientboundHotbarSyncPacket.ITEMS_LENGTH];

            boolean updatedHotbar = false;
            boolean updatedInventory = false;

            for (int i = 0; i < ClientboundInventorySyncPacket.ITEMS_LENGTH; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null) {
                    item = ItemStack.empty();
                }

                if (!item.equals(slots[i])) {
                    slots[i] = item.clone();

                    inventorySendSlots[i] = item;
                    updatedInventory = true;

                    if (i < ClientboundHotbarSyncPacket.ITEMS_LENGTH) {
                        hotbarSendSlots[i] = item;
                        updatedHotbar = true;
                    }
                }
            }

            if (updatedInventory) {
                this.plugin.getSyncController().getScreenSyncHandler().updatePlayerInventory(player, inventorySendSlots);
            }

            if (updatedHotbar) {
                this.plugin.getSyncController().broadcastPacketToSpectators(player, HOTBAR_PERMISSION, new ClientboundHotbarSyncPacket(player.getUniqueId(), hotbarSendSlots));
            }
        }
    }

    public void sendInventory(Player spectator, PlayerInventory inventory, int containerId) {
        final ItemStack[] slots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        for (int slot = 0; slot < slots.length; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) {
                item = ItemStack.empty();
            }

            slots[slot] = item;
        }

        this.plugin.getSyncController().sendPacket(spectator, new ClientboundInventorySyncPacket(inventory.getHolder().getUniqueId(), containerId, slots));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(HOTBAR_PERMISSION)) {
            final ItemStack[] slots = new ItemStack[ClientboundHotbarSyncPacket.ITEMS_LENGTH];
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
        this.playerInventories.remove(event.getPlayer().getUniqueId());
    }
}
