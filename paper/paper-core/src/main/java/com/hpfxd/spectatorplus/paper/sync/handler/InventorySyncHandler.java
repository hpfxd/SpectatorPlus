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

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

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

            // Main inventory (0-35)
            for (int i = 0; i < 36; i++) {
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
            // Armor (36-39)
            for (int i = 0; i < 4; i++) {
                ItemStack item = player.getInventory().getArmorContents()[i];
                if (item == null) {
                    item = ItemStack.empty();
                }
                int idx = 36 + i;
                if (!item.equals(slots[idx])) {
                    slots[idx] = item.clone();
                    inventorySendSlots[idx] = item;
                    updatedInventory = true;
                }
            }
            // Offhand (slot 40)
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand == null) {
                offhand = ItemStack.empty();
            }
            if (!offhand.equals(slots[40])) {
                slots[40] = offhand.clone();
                inventorySendSlots[40] = offhand;
                updatedInventory = true;
            }

            if (updatedInventory) {
                this.plugin.getSyncController().getScreenSyncHandler().updatePlayerInventory(player, inventorySendSlots);
            }

            if (updatedHotbar) {
                this.plugin.getSyncController().broadcastPacketToSpectators(player, HOTBAR_PERMISSION, new ClientboundHotbarSyncPacket(player.getUniqueId(), hotbarSendSlots));
            }
        }
    }

    public void sendInventory(Player spectator, PlayerInventory inventory) {
        final ItemStack[] slots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        // Main inventory (0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                item = ItemStack.empty();
            }
            slots[i] = item;
        }
        // Armor (36-39)
        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < 4; i++) {
            ItemStack item = armor[i];
            if (item == null) {
                item = ItemStack.empty();
            }
            slots[36 + i] = item;
        }
        // Offhand (slot 40)
        ItemStack offhand = inventory.getItemInOffHand();
        if (offhand == null) {
            offhand = ItemStack.empty();
        }
        slots[40] = offhand;
        this.plugin.getSyncController().sendPacket(spectator, new ClientboundInventorySyncPacket(inventory.getHolder().getUniqueId(), slots));
    }

    public void sendArmour(Player spectator, PlayerInventory inventory) {
        final ItemStack[] slots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        Arrays.fill(slots, ItemStack.empty());
        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < 4; i++) {
            ItemStack item = armor[i];
            if (item == null) {
                item = ItemStack.empty();
            }
            slots[36 + i] = item;
        }
        // Send the custom slots array directly as a packet
        this.plugin.getSyncController().sendPacket(spectator, new ClientboundInventorySyncPacket(inventory.getHolder().getUniqueId(), slots));
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
        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(INVENTORY_PERMISSION)) {
            // Send only the armor slots to the spectator
            this.sendArmour(spectator, target.getInventory());
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        // Send updated armor to all spectators with permission
        Player player = event.getPlayer();
        for (Player spectator : this.plugin.getSyncController().getSpectators(player, INVENTORY_PERMISSION)) {
            this.sendArmour(spectator, player.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        // If the damaged item is armor, send updated armor to all spectators with permission
        Player player = event.getPlayer();
        ItemStack damaged = event.getItem();
        // Check if the damaged item is currently equipped as armor
        boolean isArmor = false;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.equals(damaged)) {
                isArmor = true;
                break;
            }
        }
        if (isArmor) {
            for (Player spectator : this.plugin.getSyncController().getSpectators(player, INVENTORY_PERMISSION)) {
                this.sendArmour(spectator, player.getInventory());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerInventories.remove(event.getPlayer().getUniqueId());
    }
}
