package com.hpfxd.spectatorplus.paper.config;

import org.bukkit.configuration.ConfigurationSection;

public class ServerConfig {
    public final boolean workaroundTeleportTicker;
    public final boolean workaroundTeleportOnUntrack;
    public final boolean workaroundsAllowFallback;

    public ServerConfig(ConfigurationSection config) {
        this.workaroundTeleportTicker = config.getBoolean("workarounds.auto-update-position");
        this.workaroundTeleportOnUntrack = config.getBoolean("workarounds.auto-teleport-on-untrack");
        this.workaroundsAllowFallback = config.getBoolean("workarounds.allow-fallback");
    }
}
