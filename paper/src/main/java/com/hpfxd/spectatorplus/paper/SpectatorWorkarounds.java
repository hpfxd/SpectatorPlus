package com.hpfxd.spectatorplus.paper;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.util.ReflectionUtil;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUntrack(PlayerUntrackEntityEvent event) {
        final Player spectator = event.getPlayer();
        final Entity target = event.getEntity();

        if (!target.equals(spectator.getSpectatorTarget())) {
            return;
        }

        // the target has been untracked by the spectator. this is usually caused by the target teleporting a long
        // distance. so here, we need to teleport the spectator to the target, and wait for the PlayerTrackEntityEvent
        // and re-apply the spectator target.
        // this would be a lot simpler if Paper let us cancel the PlayerUntrackEntityEvent

        this.tempTargets.put(spectator.getUniqueId(), target.getUniqueId());

        if (!this.reflectionFailed) {
            try {
                ReflectionUtil.directTeleport(spectator, target.getLocation());
            } catch (ReflectiveOperationException e) {
                this.reflectionFailed = true;
            }
        }

        if (this.reflectionFailed) {
            spectator.teleport(target, PlayerTeleportEvent.TeleportCause.SPECTATE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrack(PlayerTrackEntityEvent event) {
        final Player spectator = event.getPlayer();
        final Entity target = event.getEntity();

        if (this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId()) && !event.isCancelled()) {
            // we need to schedule the re-apply for a tick later, as the target is not actually tracked yet when
            // PlayerTrackEntityEvent is called.
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                spectator.setSpectatorTarget(null);
                spectator.setSpectatorTarget(target);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStopSpectating(PlayerStopSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();
        final Entity target = event.getSpectatorTarget();

        // the spectator has stopped spectating the target. so we don't want to re-apply if the target is tracked again.
        this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId());
    }
}
