package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract SpectatorGui getSpectatorGui();
    @Shadow protected abstract void renderItemHotbar(GuiGraphics guiGraphics, float partialTick);
    @Shadow protected abstract void renderSelectedItemName(GuiGraphics guiGraphics);

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean spectatorplus$renderScoping(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.isScoping();
        }
        return instance.isScoping();
    }

    @ModifyReceiver(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getArmor(I)Lnet/minecraft/world/item/ItemStack;"))
    private Inventory spectatorplus$renderPumpkinOverlay(Inventory instance, int slot) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getInventory();
        }
        return instance;
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private int spectatorplus$renderFreezeOverlay(LocalPlayer instance) {
        return this.minecraft.getCameraEntity().getTicksFrozen();
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getPercentFrozen()F"))
    private float spectatorplus$renderFreezeOverlayPercent(LocalPlayer instance) {
        return this.minecraft.getCameraEntity().getPercentFrozen();
    }

    @Inject(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void spectatorplus$renderHotbar(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (!this.getSpectatorGui().isMenuActive() && !this.minecraft.options.hideGui) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            spectatedRef.set(spectated);

            if (spectated != null) {
                if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && !spectated.isSpectator() && SpectatorClientMod.config.renderHotbar) {
                    this.renderItemHotbar(guiGraphics, partialTick);
                }

                this.renderSelectedItemName(guiGraphics);
            }
        }
    }

    @ModifyExpressionValue(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$renderHealth(boolean original, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (original) {
            return true;
        }

        final AbstractClientPlayer spectated = spectatedRef.get();
        return spectated != null && !spectated.isCreative() && !spectated.isSpectator() && this.spectatorplus$isStatusEnabled();
    }

    @Redirect(method = "isExperienceBarVisible()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$renderExperience(MultiPlayerGameMode instance, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        final AbstractClientPlayer spectated = spectatedRef.get();
        if (spectated != null) {
            return !spectated.isCreative() && !spectated.isSpectator() && ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && this.spectatorplus$isStatusEnabled();
        }

        return instance.hasExperience();
    }

    @Unique
    private boolean spectatorplus$isStatusEnabled() {
        if (!SpectatorClientMod.config.renderStatus) {
            return false;
        }

        return SpectatorClientMod.config.renderStatusIfNoHotbar || (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1);
    }

    @Inject(method = "canRenderCrosshairForSpectator(Lnet/minecraft/world/phys/HitResult;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void spectatorplus$renderCrosshair(HitResult rayTrace, CallbackInfoReturnable<Boolean> cir) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            cir.setReturnValue(!spectated.isSpectator());
        }
    }

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    private float spectatorplus$fixCrosshairAttackStrength(LocalPlayer instance, float adjustTicks) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getAttackStrengthScale(adjustTicks);
        }
        return instance.getAttackStrengthScale(adjustTicks);
    }

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCurrentItemAttackStrengthDelay()F"))
    private float spectatorplus$fixCrosshairCurrentItemAttackStrengthDelay(LocalPlayer instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getCurrentItemAttackStrengthDelay();
        }
        return instance.getCurrentItemAttackStrengthDelay();
    }

    @Redirect(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$moveHeldItemTooltipUp(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null && !spectated.isCreative() && !spectated.isSpectator()) {
            return true;
        }
        return instance.canHurtPlayer();
    }

    @ModifyConstant(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", constant = @Constant(intValue = 39))
    private int spectatorplus$moveHealthDown(int constant) {
        if ((ClientSyncController.syncData == null || ClientSyncController.syncData.selectedHotbarSlot == -1) && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            // hotbar sync data not present, shift health down
            return constant - 27;
        }
        return constant;
    }

    @ModifyConstant(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", constant = @Constant(intValue = 10), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getPlayerVehicleWithHealth()Lnet/minecraft/world/entity/LivingEntity;"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;getSaturationLevel()F")
    ))
    private int spectatorplus$hideNonSyncedFood(int constant) {
        if ((ClientSyncController.syncData == null || ClientSyncController.syncData.foodData == null) && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return 0;
        }
        return constant;
    }

    @Redirect(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedItems(Inventory inventory) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.selectedHotbarSlot;
        }
        return inventory.selected;
    }

    @Redirect(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"))
    private FoodData spectatorplus$showSyncedFood(Player instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.foodData;
        }
        return instance.getFoodData();
    }

    @ModifyReceiver(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;", ordinal = 0))
    private NonNullList<ItemStack> spectatorplus$showSyncedSelectedSlot(NonNullList<ItemStack> instance, int i) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.hotbarItems;
        }
        return instance;
    }

    @Redirect(method = "renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXpNeededForNextLevel()I"))
    private int spectatorplus$showSyncedExperienceBar(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceNeededForNextLevel;
        }
        return instance.getXpNeededForNextLevel();
    }

    @Redirect(method = "renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float spectatorplus$showSyncedExperienceProgress(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceProgress;
        }
        return instance.experienceProgress;
    }

    @Redirect(method = "renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedExperienceLevel(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceLevel;
        }
        return instance.experienceLevel;
    }

    @ModifyReceiver(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelected()Lnet/minecraft/world/item/ItemStack;"))
    private Inventory spectatorplus$modifyTooltipTick(Inventory instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getInventory();
        }
        return instance;
    }
}
