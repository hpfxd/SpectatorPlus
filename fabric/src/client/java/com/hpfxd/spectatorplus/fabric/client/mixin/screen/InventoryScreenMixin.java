package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryMenu;
import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryScreen;
import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    public InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Player;inventoryMenu:Lnet/minecraft/world/inventory/InventoryMenu;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private static InventoryMenu spectatorplus$useCustomInventoryMenu(Player instance) {
        // I'm not aware of a way to check if this is an instance of SyncedInventoryScreen, otherwise I would here

        if (ScreenSyncController.isPendingOpen && ScreenSyncController.syncedInventory != null) {
            return new SyncedInventoryMenu(ScreenSyncController.syncedInventory, true, instance);
        }
        return instance.inventoryMenu;
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getInventory()Lnet/minecraft/world/entity/player/Inventory;"
            )
    )
    private static Inventory spectatorplus$modifySyncedInventory(Player instance) {
        // I'm not aware of a way to check if this is an instance of SyncedInventoryScreen, otherwise I would here

        if (ScreenSyncController.isPendingOpen && ScreenSyncController.syncedInventory != null) {
            return ScreenSyncController.syncedInventory;
        }
        return instance.getInventory();
    }

    @Redirect(
            method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void spectatorplus$renderTargetPlayerInInventory(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale, float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        if (((Object) this) instanceof SyncedInventoryScreen) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
            if (spectated != null) {
                entity = spectated;
            }
        }
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x1, y1, x2, y2, scale, yOffset, mouseX, mouseY, entity);
    }
}
