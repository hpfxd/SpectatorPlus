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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectatorWorkarounds implements Listener {
    private final SpectatorPlugin plugin;

    private final Map<UUID, UUID> tempTargets = new HashMap<>();
    private boolean directTeleportFailed;
    private boolean cameraPacketFailed;

    public SpectatorWorkarounds(SpectatorPlugin plugin) {
        this.plugin = plugin;

        if (plugin.getServerConfig().workaroundTeleportTicker) {
            Bukkit.getScheduler().runTaskTimer(plugin, this::updateSpectatorPositions, 20, 20);
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void updateSpectatorPositions() {
        for (final Player spectator : Bukkit.getOnlinePlayers()) {
            final Entity target = spectator.getSpectatorTarget();

            if (target != null) {
                if (spectator.getWorld().equals(target.getWorld())) {
                    if (!this.directTeleportFailed) {
                        try {
                            ReflectionUtil.directTeleport(spectator, target.getLocation());
                        } catch (Throwable e) {
                            this.directTeleportFailed = true;
                            this.plugin.getSLF4JLogger().warn("auto-update-position workaround: Failed to call directTeleport, will not try again", e);
                            if (this.plugin.getServerConfig().workaroundsAllowFallback) {
                                this.plugin.getSLF4JLogger().warn("\"allow-fallback\" is enabled in the plugin configuration. This has a few drawbacks, it is recommended to view the notes in the config about this option.");
                            }
                        }
                    }

                    if (this.directTeleportFailed && this.plugin.getServerConfig().workaroundsAllowFallback) {
                        spectator.setSpectatorTarget(null);
                        spectator.setSpectatorTarget(target);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUntrack(PlayerUntrackEntityEvent event) {
        if (!this.plugin.getServerConfig().workaroundTeleportOnUntrack) {
            return;
        }

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

        if (!this.directTeleportFailed) {
            try {
                ReflectionUtil.directTeleport(spectator, target.getLocation());
            } catch (Throwable e) {
                this.directTeleportFailed = true;
            }
        }

        if (this.directTeleportFailed && this.plugin.getServerConfig().workaroundsAllowFallback) {
            spectator.teleport(target, PlayerTeleportEvent.TeleportCause.SPECTATE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrack(PlayerTrackEntityEvent event) {
        if (!this.plugin.getServerConfig().workaroundTeleportOnUntrack) {
            return;
        }

        final Player spectator = event.getPlayer();
        final Entity target = event.getEntity();

        if (this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId()) && !event.isCancelled()) {
            // we need to schedule the re-apply for a tick later, as the target is not actually tracked yet when
            // PlayerTrackEntityEvent is called.
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (!this.cameraPacketFailed) {
                    try {
                        // attempt to send ClientboundSetCameraPacket directly to the spectator as that's all that is really
                        // needed, and we can try to skip the logic in setSpectatorTarget() which includes teleporting and
                        // calling PlayerStartSpectatingEntityEvent.
                        ReflectionUtil.sendCameraPacket(spectator, target);
                    } catch (Throwable e) {
                        this.cameraPacketFailed = true;
                        this.plugin.getSLF4JLogger().warn("auto-teleport-on-untrack workaround: Failed to send ClientboundSetCameraPacket directly", e);
                        if (this.plugin.getServerConfig().workaroundsAllowFallback) {
                            this.plugin.getSLF4JLogger().warn("\"allow-fallback\" is enabled in the plugin configuration, falling back to Bukkit setSpectatorTarget(). This is unlikely to cause issues.");
                        }
                    }
                }

                if (this.cameraPacketFailed) {
                    spectator.setSpectatorTarget(null);
                    spectator.setSpectatorTarget(target);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStopSpectating(PlayerStopSpectatingEntityEvent event) {
        if (!this.plugin.getServerConfig().workaroundTeleportOnUntrack) {
            return;
        }

        final Player spectator = event.getPlayer();
        final Entity target = event.getSpectatorTarget();

        // the spectator has stopped spectating the target. so we don't want to re-apply if the target is tracked again.
        this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.tempTargets.remove(event.getPlayer().getUniqueId());
    }
}
