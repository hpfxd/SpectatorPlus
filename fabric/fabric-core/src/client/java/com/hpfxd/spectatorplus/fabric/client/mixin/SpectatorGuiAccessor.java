package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpectatorGui.class)
public interface SpectatorGuiAccessor {
    @Accessor
    SpectatorMenu getMenu();

    @Accessor
    void setLastSelectionTime(long millis);
}
