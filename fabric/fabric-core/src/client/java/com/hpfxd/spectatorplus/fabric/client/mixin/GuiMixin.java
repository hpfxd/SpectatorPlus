package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator.Context.Local;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;

@Mixin(Gui.class)
public abstract class GuiMixin {
    // Local copy of vanilla overlay resource location
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract SpectatorGui getSpectatorGui();
    @Shadow protected abstract void renderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
    @Shadow protected abstract void renderPortalOverlay(GuiGraphics guiGraphics, float intensity);

    @Shadow @Final private SpectatorGui spectatorGui;


    // Use correct Identifiers for vanilla empty armor slot icons from the GUI atlas
    private static final Identifier EMPTY_ARMOR_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_ARMOR_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EFFECT_BACKGROUND_AMBIENT_SPRITE = Identifier.withDefaultNamespace("hud/effect_background_ambient");
    private static final Identifier EFFECT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/effect_background");

    private static final Identifier[] TEXTURE_EMPTY_SLOTS = new Identifier[]{
        EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET
    };

    @Inject(
        method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void spectatorplus$cancelRenderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean spectatorplus$renderScoping(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.isScoping();
        }
        return instance.isScoping();
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$renderItemCameraOverlay(LocalPlayer instance, EquipmentSlot slot) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getItemBySlot(slot);
        }
        return instance.getItemBySlot(slot);
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private int spectatorplus$renderFreezeOverlay(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getTicksFrozen();
        }
        return instance.getTicksFrozen();
    }

    @Redirect(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getPercentFrozen()F"))
    private float spectatorplus$renderFreezeOverlayPercent(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getPercentFrozen();
        }
        return instance.getPercentFrozen();
    }

    @Inject(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void spectatorplus$renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (!this.getSpectatorGui().isMenuActive() && !this.minecraft.options.hideGui) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            spectatedRef.set(spectated);

            if (spectated != null) {
                if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && !spectated.isSpectator() && SpectatorClientMod.config.renderHotbar) {
                    this.renderItemHotbar(guiGraphics, deltaTracker);
                }

                // Render all spectatee's armor in the top right: helmet, chestplate, leggings, boots
                if (ClientSyncController.syncData != null && ClientSyncController.syncData.armorItems != null) {
                    int spacing = 1; // vertical spacing between items
                    int itemWidth = 16; // standard item icon width
                    int itemHeight = 16; // standard item icon height
                    int baseY = 2;
                    int baseX = this.minecraft.getWindow().getGuiScaledWidth() - itemWidth - 4;
                    // Armor order: helmet, chestplate, leggings, boots (reverse to boots, leggings, chestplate, helmet)
                    EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                    for (int i = 0; i < slots.length; i++) {
                        int idx = ClientSyncController.syncData.armorItems.size() - 1 - i;
                        ItemStack armorStack = idx >= 0 && idx < ClientSyncController.syncData.armorItems.size() ? ClientSyncController.syncData.armorItems.get(idx) : ItemStack.EMPTY;
                        int y = baseY + i * (itemHeight + spacing);
                        boolean isAir = armorStack == null || armorStack.isEmpty() || armorStack.getItem() == net.minecraft.world.item.Items.AIR;
                        if (isAir) {
                            // Show empty slot icon if no item
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE_EMPTY_SLOTS[idx], baseX, y, itemWidth, itemHeight);
                        } else {
                            // Show item icon if present
                            guiGraphics.renderItem(armorStack, baseX, y);
                            // Draw durability % if item is damageable
                            if (armorStack.isDamageableItem() && armorStack.getMaxDamage() > 0) {
                                int durability = armorStack.getMaxDamage() - armorStack.getDamageValue();
                                int percent = (int) ((durability * 100.0) / armorStack.getMaxDamage());
                                String numText = String.valueOf(percent);
                                String percentChar = "%";
                                int numColor;
                                if (percent == 100) {
                                    numColor = 0xFF00FF00; // lime
                                } else if (percent < 10) {
                                    numColor = 0xFFFF0000; // red
                                } else if (percent < 25) {
                                    numColor = 0xFFFFA500; // orange
                                } else {
                                    numColor = 0xFFFFFFFF; // white
                                }
                                // Right-align the text to the left of the icon
                                int numTextWidth = this.minecraft.font.width(numText);
                                int percentTextWidth = this.minecraft.font.width(percentChar);
                                int textWidth = numTextWidth + percentTextWidth;
                                int textX = baseX - spacing - textWidth; // right-aligned to the left of the item
                                int textY = y + 4; // vertically centered
                                // Draw numeric part
                                guiGraphics.drawString(this.minecraft.font, numText, textX, textY, numColor, true);
                                // Draw '%' in white
                                guiGraphics.drawString(this.minecraft.font, percentChar, textX + numTextWidth, textY, 0xFFFFFFFF, true);
                            }
                        }
                    }

                    int effectBaseY = baseY + slots.length * (itemHeight + spacing) + spacing; // start below armor

                    // Render all active effect icons down the right side below armor
                    if (ClientSyncController.syncData.effects != null && !ClientSyncController.syncData.effects.isEmpty()) {
                        int effectIndex = 0;
                        for (var effectInstance : ClientSyncController.syncData.effects) {
                            int y = effectBaseY + effectIndex * (itemWidth + spacing);

                            // Draw vanilla effect background
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, baseX, y, itemWidth, itemHeight);

                            Identifier effectIcon = GuiMixin.getEffectIcon(effectInstance.effectKey);
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, effectIcon, baseX + 2, y + 2, itemWidth - 4, itemHeight - 4);

                            // Draw effect level as a small white number on the top right of the icon
                            int level = effectInstance.amplifier + 1;
                            String levelText = String.valueOf(level);
                            int levelTextWidth = this.minecraft.font.width(levelText);
                            int levelTextX = baseX + itemWidth - (int)(levelTextWidth * 0.4F) - 3; // right-align inside top-right corner
                            int levelTextY = y + 2;
                            guiGraphics.pose().pushMatrix();
                            guiGraphics.pose().scale(0.5F, 0.5F);
                            guiGraphics.drawString(this.minecraft.font, levelText, (int)(levelTextX / 0.5F), (int)(levelTextY / 0.5F), 0xFFFFFFFF, true);
                            guiGraphics.pose().popMatrix();

                            // Draw duration bar (1px wide) to the left of the effect icon, color changes with percent
                            int duration = effectInstance.duration;
                            int maxDuration = 3600; // 3 minutes, adjust as needed
                            float percent = maxDuration > 0 ? (duration / (float)maxDuration) : 1.0F;
                            int maxBarHeight = itemHeight - 2;
                            int barHeight = Math.min(maxBarHeight, (int)(maxBarHeight * percent));
                            int barX = baseX + 1; // 1px left of icon
                            int barY = y + itemHeight - 1 - barHeight; // 1px up from bottom
                            int barColor;
                            if (percent > 0.20F) {
                                barColor = 0xFF00FF00; // green
                            } else if (percent > 0.05F) {
                                barColor = 0xFFFFA500; // orange
                            } else {
                                barColor = 0xFFFF0000; // red
                            }
                            if (barHeight > 0) {
                                guiGraphics.fill(barX, barY, barX + 2, barY + barHeight, barColor);
                            }

                            effectIndex++;
                        }
                    }
                }
            }
        }
    }

    @ModifyExpressionValue(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$renderHealth(boolean original, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (original) {
            return true;
        }

        final AbstractClientPlayer spectated = spectatedRef.get();
        return spectated != null && !spectated.isCreative() && !spectated.isSpectator() && this.spectatorplus$isStatusEnabled();
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$renderExperience(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return !spectated.isCreative() && !spectated.isSpectator() && ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && this.spectatorplus$isStatusEnabled();
        }

        return instance.hasExperience();
    }

    @Redirect(method = "nextContextualInfoState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$hasExperience(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
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

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    private float spectatorplus$fixCrosshairAttackStrength(LocalPlayer instance, float adjustTicks) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getAttackStrengthScale(adjustTicks);
        }
        return instance.getAttackStrengthScale(adjustTicks);
    }

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCurrentItemAttackStrengthDelay()F"))
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

    @WrapWithCondition(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
    private boolean spectatorplus$hideNonSyncedFood(Gui instance, GuiGraphics guiGraphics, Player player, int y, int x) {
        return (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null) || SpecUtil.getCameraPlayer(this.minecraft) == null;
    }

    @Redirect(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedSlot()I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedSelectedSlot(Inventory inventory) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.selectedHotbarSlot;
        }
        return inventory.getSelectedSlot();
    }

    @Redirect(method = "renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"))
    private FoodData spectatorplus$showSyncedFood(Player instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.foodData;
        }
        return instance.getFoodData();
    }

    @Redirect(
        method = "renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean spectatorplus$showSyncedFoodSprite(Player instance, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        final LocalPlayer player = this.minecraft.player;
        if (player != null && player.hasEffect(effect)) {
            return true;
        }
        return instance.hasEffect(effect);
    }

    @Redirect(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$showSyncedItems(Inventory instance, int slot) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.hotbarItems.get(slot);
        }
        return instance.getItem(slot);
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedExperienceLevel(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceLevel;
        }
        return instance.experienceLevel;
    }

    @ModifyReceiver(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    private Inventory spectatorplus$modifyTooltipTick(Inventory instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getInventory();
        }
        return instance;
    }
    // Map EffectType to vanilla effect icon Identifier
    private static Identifier getEffectIcon(String effectKey) {
        // If effectKey contains a namespace (e.g., minecraft:nausea), strip it
        String key = effectKey;
        int colonIdx = key.indexOf(":");
        if (colonIdx != -1) {
            key = key.substring(colonIdx + 1);
        }
        // Vanilla effect icons are in the GUI atlas as effect/<effectKey>
        // The effectKey should be lowercase, matching the registry name
        return Identifier.withDefaultNamespace("mob_effect/" + key.toLowerCase());
    }

}
