package com.hpfxd.spectatorplus.fabric.client;

import com.google.common.collect.Iterables;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SpectatorKeybinds {
    private static final Comparator<PlayerInfo> PROFILE_ORDER = Comparator.comparing(playerInfo -> playerInfo.getProfile().getId());

    private static KeyMapping CLOSEST_PLAYER;
    private static KeyMapping NEXT_PLAYER;
    private static KeyMapping PREVIOUS_PLAYER;

    private static UUID targetCursorId;

    public static void init() {
        CLOSEST_PLAYER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.spectatorplus.closestPlayer",
                GLFW.GLFW_KEY_UP,
                "key.categories.spectatorplus"
        ));

        NEXT_PLAYER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.spectatorplus.nextPlayer",
                GLFW.GLFW_KEY_RIGHT,
                "key.categories.spectatorplus"
        ));

        PREVIOUS_PLAYER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.spectatorplus.previousPlayer",
                GLFW.GLFW_KEY_LEFT,
                "key.categories.spectatorplus"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SpectatorKeybinds::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            while (CLOSEST_PLAYER.consumeClick()) {
                final Entity nearest = mc.level.getNearestPlayer(mc.player.getX(), mc.player.getY(), mc.player.getZ(), 256, EntitySelector.NO_SPECTATORS.and(entity -> mc.cameraEntity != entity));

                if (nearest != null) {
                    setTarget(mc, nearest.getUUID());
                    mc.player.displayClientMessage(Component.translatable("spectatorplus.target.now-spectating", Component.empty().append(nearest.getDisplayName())
                            .withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY), true);
                } else {
                    mc.player.displayClientMessage(Component.translatable("spectatorplus.target.no-closest-player").withStyle(ChatFormatting.RED), true);
                }
            }

            while (NEXT_PLAYER.consumeClick()) {
                targetNext(mc, 1);
            }

            while (PREVIOUS_PLAYER.consumeClick()) {
                targetNext(mc, -1);
            }
        }
    }

    private static void targetNext(Minecraft mc, int shift) {
        final PlayerInfo target = shiftPlayerCursor(mc, shift);

        if (target != null && !target.getProfile().getId().equals(mc.cameraEntity.getUUID())) {
            setTarget(mc, target.getProfile().getId());
            mc.player.displayClientMessage(Component.translatable("spectatorplus.target.now-spectating", Component.empty().append(mc.gui.getTabList().getNameForDisplay(target))
                    .withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY), true);
        } else {
            mc.player.displayClientMessage(Component.translatable("spectatorplus.target.no-player").withStyle(ChatFormatting.RED), true);
        }
    }

    private static PlayerInfo shiftPlayerCursor(Minecraft mc, int shift) {
        final List<PlayerInfo> players = mc.getConnection().getListedOnlinePlayers().stream()
                .filter(playerInfo -> playerInfo.getGameMode() != GameType.SPECTATOR)
                .sorted(PROFILE_ORDER)
                .toList();

        if (players.isEmpty()) {
            return null;
        }

        int index = -1;

        if (targetCursorId != null) {
            index = Iterables.indexOf(players, player -> targetCursorId.equals(player.getProfile().getId()));
        }

        if (index == -1 && shift < 0) {
            index = 0;
        }

        index += shift;
        return players.get(Math.floorMod(index, players.size()));
    }

    private static void setTarget(Minecraft mc, UUID uuid) {
        targetCursorId = uuid;

        if (SpectatorClientMod.config.teleportAutoSpectate) {
            ClientTargetController.requestTargetFromServer(mc, uuid);
        } else {
            mc.getConnection().send(new ServerboundTeleportToEntityPacket(uuid));
        }
    }
}
