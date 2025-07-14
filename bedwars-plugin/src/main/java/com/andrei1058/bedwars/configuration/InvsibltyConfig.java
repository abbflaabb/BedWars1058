package com.andrei1058.bedwars.configuration;

import com.andrei1058.bedwars.BedWars;

public class InvsibltyConfig {

    private final BedWars plugin;

    public InvsibltyConfig(BedWars plugin) {
        this.plugin = plugin;
    }

    public boolean isWoodSwordDisappearanceEnabled() {
        return plugin.getConfig().getBoolean("enable-wood-sword-disappearance", true);
    }

    public boolean isRespawnSessionInvisibilityEnabled() {
        return plugin.getConfig().getBoolean("enable-respawn-session-invisibility", true);
    }
}