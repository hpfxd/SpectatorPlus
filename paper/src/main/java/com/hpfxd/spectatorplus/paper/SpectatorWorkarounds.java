package com.hpfxd.spectatorplus.paper;

import com.google.common.collect.Lists;
import com.hpfxd.spectatorplus.paper.util.ReflectionUtil;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectatorWorkarounds implements Listener {
    private final SpectatorPlugin plugin;

    private final Map<UUID, UUID> tempTargets = new HashMap<>();
    private boolean reflectionFailed;

    public SpectatorWorkarounds(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateSpectatorPositions, 20, 20);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void updateSpectatorPositions() {
        for (final Player spectator : Bukkit.getOnlinePlayers()) {
            final Entity target = spectator.getSpectatorTarget();

            if (target != null) {
                if (spectator.getWorld().equals(target.getWorld())) {
                    if (!this.reflectionFailed) {
                        try {
                            ReflectionUtil.directTeleport(spectator, target.getLocation());
                        } catch (ReflectiveOperationException e) {
                            // todo note side effects in log message: https://github.com/NoahvdAa/SpectatorSendChunks/#side-effects
                            this.plugin.getSLF4JLogger().warn("Failed to call directTeleport, falling back to normal Bukkit teleport", e);
                            this.reflectionFailed = true;
                        }
                    } else {
                        spectator.setSpectatorTarget(null);
                        spectator.setSpectatorTarget(target);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        final Player target = event.getPlayer();

        final Collection<Player> spectators = Lists.newArrayList(this.plugin.getSyncController().getSpectators(target));

        if (!spectators.isEmpty()) {
            // TODO: don't re-apply spectator if didn't teleport far away
            // Bukkit.getScheduler().runTask()

            for (final Player spectator : spectators) {
                spectator.setSpectatorTarget(null);
            }

            spectators.removeIf(spectator -> !spectator.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.SPECTATE));

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                for (final Player spectator : spectators) {
                    if (target.getTrackedBy().contains(spectator)) {
                        spectator.setSpectatorTarget(target);
                    } else {
                        this.plugin.getSLF4JLogger().info("MC-107113 workaround: Queueing spectator re-apply for {}", spectator.getName());
                        this.tempTargets.put(spectator.getUniqueId(), target.getUniqueId());
                    }
                }
            }, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrack(PlayerTrackEntityEvent event) {
        final Player spectator = event.getPlayer();

        final Entity target = event.getEntity();
        if (this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId())) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                spectator.setSpectatorTarget(null);
                spectator.setSpectatorTarget(target);
            });
        }
    }
}
