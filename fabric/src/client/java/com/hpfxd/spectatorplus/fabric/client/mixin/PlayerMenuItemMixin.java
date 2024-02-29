package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.ClientTargetController;
import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMenuItem.class)
public class PlayerMenuItemMixin {
    @Shadow @Final private GameProfile profile;

    @Inject(method = "selectItem(Lnet/minecraft/client/gui/spectator/SpectatorMenu;)V", at = @At("HEAD"), cancellable = true)
    private void spectatorplus$handleSelect(SpectatorMenu menu, CallbackInfo ci) {
        if (SpectatorClientMod.config.teleportAutoSpectate) {
            ClientTargetController.requestTargetFromServer(Minecraft.getInstance(), this.profile.getId());
            ci.cancel();
        }
    }
}
