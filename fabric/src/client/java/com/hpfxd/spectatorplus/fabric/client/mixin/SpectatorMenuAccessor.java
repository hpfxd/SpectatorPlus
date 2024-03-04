package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.gui.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpectatorMenu.class)
public interface SpectatorMenuAccessor {
    @Accessor
    int getPage();

    @Accessor
    void setPage(int page);
}
