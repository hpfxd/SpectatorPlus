package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundExperienceSyncPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ExperienceSyncHandler implements Listener {
    private static final String PERMISSION = "spectatorplus.sync.experience";

    private final SpectatorPlugin plugin;
    private final Object2IntMap<UUID> playerExperience = new Object2IntOpenHashMap<>();

    public ExperienceSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 0);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void tick() {
        // unfortunately the PlayerExpChangeEvent does not capture all experience changes, so we need to track players
        // in every tick and compare to the amount in the last tick in order to be accurate.

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final int current = player.calculateTotalExperiencePoints();
            final int old = this.playerExperience.put(player.getUniqueId(), current);

            if (current != old) {
                this.plugin.getSyncController().broadcastPacketToSpectators(player, PERMISSION, new ClientboundExperienceSyncPacket(player.getUniqueId(), player.getExp(), current, player.getLevel()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            this.plugin.getSyncController().sendPacket(spectator, new ClientboundExperienceSyncPacket(target.getUniqueId(), target.getExp(), target.getExperiencePointsNeededForNextLevel(), target.getLevel()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerExperience.removeInt(event.getPlayer().getUniqueId());
    }
}
