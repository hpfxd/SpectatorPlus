package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundEffectsSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.*;

public class EffectsSyncHandler {
    private static final Map<UUID, List<SyncedEffect>> EFFECTS = new HashMap<>();

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> EFFECTS.remove(handler.getPlayer().getUUID()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> EFFECTS.clear());
    }

    // 当玩家开始旁观另一个玩家时调用
    public static void onStartSpectating(ServerPlayer spectator, ServerPlayer target) {
        syncPlayerEffects(spectator, target);
    }

    // 当玩家的药水效果改变时调用
    public static void onEffectChanged(ServerPlayer player) {
        // 获取所有正在旁观此玩家的观察者
        for (ServerPlayer spectator : ServerSyncController.getSpectators(player)) {
            syncPlayerEffects(spectator, player);
        }
    }

    private static void syncPlayerEffects(ServerPlayer spectator, ServerPlayer target) {
        final List<SyncedEffect> currentEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : target.getActiveEffects()) {
            String effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value()).toString();
            currentEffects.add(new SyncedEffect(
                effectKey,
                effectInstance.getAmplifier(),
                effectInstance.getDuration()
            ));
        }

        final List<SyncedEffect> cachedEffects = EFFECTS.computeIfAbsent(spectator.getUUID(), k -> new ArrayList<>());

        if (!effectsEqual(currentEffects, cachedEffects)) {
            cachedEffects.clear();
            cachedEffects.addAll(currentEffects);

            // 使用ServerPlayNetworking发送包
            ClientboundEffectsSyncPacket packet = new ClientboundEffectsSyncPacket(target.getUUID(), new ArrayList<>(currentEffects));
            if (packet.canSend(spectator)) {
                ServerPlayNetworking.send(spectator, packet);
            }
        }
    }

    private static boolean effectsEqual(List<SyncedEffect> list1, List<SyncedEffect> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        final Map<String, SyncedEffect> map1 = new HashMap<>();
        final Map<String, SyncedEffect> map2 = new HashMap<>();

        for (SyncedEffect effect : list1) {
            map1.put(effect.effectKey, effect);
        }

        for (SyncedEffect effect : list2) {
            map2.put(effect.effectKey, effect);
        }

        return map1.equals(map2);
    }
}