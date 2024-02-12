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
    private boolean directTeleportFailed;
    private boolean cameraPacketFailed;

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
                    if (!this.directTeleportFailed) {
                        try {
                            ReflectionUtil.directTeleport(spectator, target.getLocation());
                        } catch (Throwable e) {
                            // todo note side effects in log message: https://github.com/NoahvdAa/SpectatorSendChunks/#side-effects
                            this.plugin.getSLF4JLogger().warn("Failed to call directTeleport, falling back to normal Bukkit teleport", e);
                            this.directTeleportFailed = true;
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

        if (!this.directTeleportFailed) {
            try {
                ReflectionUtil.directTeleport(spectator, target.getLocation());
            } catch (Throwable e) {
                this.directTeleportFailed = true;
            }
        }

        if (this.directTeleportFailed) {
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
                if (!this.cameraPacketFailed) {
                    try {
                        // attempt to send ClientboundSetCameraPacket directly to the spectator as that's all that is really
                        // needed, and we can try to skip the logic in setSpectatorTarget() which includes teleporting and
                        // calling PlayerStartSpectatingEntityEvent.
                        ReflectionUtil.sendCameraPacket(spectator, target);
                    } catch (Throwable e) {
                        this.plugin.getSLF4JLogger().warn("Failed to send ClientboundSetCameraPacket directly, falling back to Bukkit setSpectatorTarget(). This is okay and unlikely to cause issues.", e);
                        this.cameraPacketFailed = true;
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
        final Player spectator = event.getPlayer();
        final Entity target = event.getSpectatorTarget();

        // the spectator has stopped spectating the target. so we don't want to re-apply if the target is tracked again.
        this.tempTargets.remove(spectator.getUniqueId(), target.getUniqueId());
    }
}
