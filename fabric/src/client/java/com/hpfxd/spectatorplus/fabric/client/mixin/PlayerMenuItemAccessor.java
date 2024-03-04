package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMenuItem.class)
public interface PlayerMenuItemAccessor {
    @Accessor
    GameProfile getProfile();
}
