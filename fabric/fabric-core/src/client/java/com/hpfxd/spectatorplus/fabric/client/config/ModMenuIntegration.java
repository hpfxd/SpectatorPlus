package com.hpfxd.spectatorplus.fabric.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                return ClothConfigIntegration.getConfigScreen(parent);
            }
            return null;
        };
    }
}
