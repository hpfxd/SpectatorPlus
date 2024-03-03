package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

/**
 * Handles syncing map item data to spectators.
 * <p>
 * When starting to spectate a player, the target's inventory is scanned for all maps and data is all sent to the
 * spectator. Additionally, this is repeated every {@link MapSyncHandler#MAP_UPDATE_FREQUENCY} ticks for all players.
 * <p>
 * The {@code ClientboundMapItemDataPacket} has the ability to only re-send sections of maps which have changed since
 * the last packet, but Bukkit does not have a reliable way for us to construct this and send it to spectators. Likely
 * the best way to do this within the platform restrictions would be to inject into the Netty pipeline for spectated
 * players and listen for the map packet, and send it to spectators directly there. This is not currently implemented.
 * <p>
 * Alternatively, the plugin could somehow detect when maps have been updated and re-send map data then instead of
 * always at a fixed rate. The problem is that there is no way to send partial updates to the client, and something
 * as simple as a player marker update which happens very often would need a full re-send anyway.
 */
public class MapSyncHandler implements Listener {
    /**
     * How often to re-send all maps to all spectators.
     */
    private static final int MAP_UPDATE_FREQUENCY = 30 * 20;

    public MapSyncHandler(SpectatorPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 0, MAP_UPDATE_FREQUENCY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        if (event.getNewSpectatorTarget() instanceof final Player target) {
           this.sendMaps(target, spectator);
        }
    }

    private void updateAll() {
        for (final Player spectator : Bukkit.getOnlinePlayers()) {
            if (spectator.getSpectatorTarget() instanceof final Player target) {
                this.sendMaps(target, spectator);
            }
        }
    }

    /**
     * Send {@link MapView}s of all map items in spectated player's inventory to the spectator.
     */
    private void sendMaps(Player target, Player spectator) {
        for (final ItemStack item : target.getInventory()) {
            if (item != null && item.getType() == Material.FILLED_MAP && item.getItemMeta() instanceof final MapMeta mapMeta) {
                if (mapMeta.hasMapView()) {
                    final MapView view = mapMeta.getMapView();

                    if (view != null) {
                        spectator.sendMap(view);
                    }
                }
            }
        }
    }
}
