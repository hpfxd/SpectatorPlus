package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectsSyncHandler implements Listener {
    /**
     * Sets the target player's potion effects to match the source player's effects.
     * Removes all current effects and applies all effects from source.
     */
    private void syncPlayerEffects(Player target, Player source) {
        java.util.Map<org.bukkit.potion.PotionEffectType, PotionEffect> sourceEffects = new java.util.HashMap<>();
        //this.plugin.getSLF4JLogger().info("[SpectatorPlus] Syncing effects: setting {} effects to match {}: {}", target.getName(), source.getName(), sourceEffects);

        for (PotionEffect effect : source.getActivePotionEffects()) {
            sourceEffects.put(effect.getType(), effect);
        }

        java.util.Map<org.bukkit.potion.PotionEffectType, PotionEffect> targetEffects = new java.util.HashMap<>();
        for (PotionEffect effect : target.getActivePotionEffects()) {
            targetEffects.put(effect.getType(), effect);
        }

        // Remove effects that are not present in source or have changed
        for (PotionEffectType type : targetEffects.keySet()) {
            PotionEffect sourceEffect = sourceEffects.get(type);
            PotionEffect targetEffect = targetEffects.get(type);
            if (sourceEffect == null ||
                sourceEffect.getDuration() != targetEffect.getDuration() ||
                sourceEffect.getAmplifier() != targetEffect.getAmplifier() ||
                sourceEffect.isAmbient() != targetEffect.isAmbient() ||
                sourceEffect.hasParticles() != targetEffect.hasParticles() ||
                sourceEffect.hasIcon() != targetEffect.hasIcon()) {
                target.removePotionEffect(type);
            }
        }

        // Add effects that are new or changed
        for (PotionEffectType type : sourceEffects.keySet()) {
            PotionEffect sourceEffect = sourceEffects.get(type);
            PotionEffect targetEffect = targetEffects.get(type);
            if (targetEffect == null ||
                sourceEffect.getDuration() != targetEffect.getDuration() ||
                sourceEffect.getAmplifier() != targetEffect.getAmplifier() ||
                sourceEffect.isAmbient() != targetEffect.isAmbient() ||
                sourceEffect.hasParticles() != targetEffect.hasParticles() ||
                sourceEffect.hasIcon() != targetEffect.hasIcon()) {
                target.addPotionEffect(new PotionEffect(
                    sourceEffect.getType(),
                    sourceEffect.getDuration(),
                    sourceEffect.getAmplifier(),
                    sourceEffect.isAmbient(),
                    sourceEffect.hasParticles(),
                    sourceEffect.hasIcon()
                ));
            }
        }
    }
    private static final String PERMISSION = "spectatorplus.sync.effects";

    private final SpectatorPlugin plugin;

    public EffectsSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();
        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            syncPlayerEffects(spectator, target);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                for (Player spectator : Bukkit.getOnlinePlayers()) {
                    if (spectator != player && spectator.hasPermission(PERMISSION) && spectator.getSpectatorTarget() == player) {
                        syncPlayerEffects(spectator, player);
                    }
                }
            });
        }
    }
}
