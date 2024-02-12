package com.hpfxd.spectatorplus.paper.util;

import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class ReflectionUtil {
    private static final ReflectionRemapper REMAPPER;

    private static Method CraftEntity$getHandle;
    private static Field ServerPlayer$connection;
    private static Method ServerGamePacketListenerImpl$internalTeleport;
    private static Method ServerCommonPacketListenerImpl$send;
    private static Constructor<?> ClientboundSetCameraPacket$init;

    static {
        try {
            REMAPPER = ReflectionRemapper.forReobfMappingsInPaperJar();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Paper reobf mappings", e);
        }

        JavaPlugin.getPlugin(SpectatorPlugin.class).getSLF4JLogger().info("Loaded reflection mappings: " + REMAPPER.getClass().getSimpleName());
    }

    private ReflectionUtil() {
    }

    private static Object getHandle(Entity entity) throws ReflectiveOperationException {
        if (CraftEntity$getHandle == null) {
            CraftEntity$getHandle = entity.getClass().getMethod("getHandle");
        }
        return CraftEntity$getHandle.invoke(entity);
    }

    private static Object getConnection(Object serverPlayer) throws ReflectiveOperationException {
        if (ServerPlayer$connection == null) {
            final Class<?> clazz = serverPlayer.getClass();
            ServerPlayer$connection = clazz.getField(REMAPPER.remapFieldName(clazz, "connection"));
        }
        return ServerPlayer$connection.get(serverPlayer);
    }

    private static void internalTeleport(Object connection, Location location) throws ReflectiveOperationException {
        if (ServerGamePacketListenerImpl$internalTeleport == null) {
            final Class<?> clazz = connection.getClass();
            final Class<?>[] parameterTypes = new Class[]{double.class, double.class, double.class, float.class, float.class, Set.class};
            ServerGamePacketListenerImpl$internalTeleport = clazz.getMethod(REMAPPER.remapMethodName(clazz, "internalTeleport", parameterTypes), parameterTypes);
        }
        ServerGamePacketListenerImpl$internalTeleport.invoke(connection, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), Set.of());
    }

    private static Object constructCameraPacket(Object minecraftEntity) throws ReflectiveOperationException {
        if (ClientboundSetCameraPacket$init == null) {
            final Class<?> entityClass = Class.forName(REMAPPER.remapClassName("net.minecraft.world.entity.Entity"));
            final Class<?> setCameraPacketClass = Class.forName(REMAPPER.remapClassName("net.minecraft.network.protocol.game.ClientboundSetCameraPacket"));

            ClientboundSetCameraPacket$init = setCameraPacketClass.getConstructor(entityClass);
        }

        return ClientboundSetCameraPacket$init.newInstance(minecraftEntity);
    }

    private static void sendPacket(Object connection, Object packet) throws ReflectiveOperationException {
        if (ServerCommonPacketListenerImpl$send == null) {
            final Class<?> packetClass = Class.forName(REMAPPER.remapClassName("net.minecraft.network.protocol.Packet"));
            final Class<?> commonListenerClass = connection.getClass().getSuperclass();

            ServerCommonPacketListenerImpl$send = commonListenerClass.getMethod(REMAPPER.remapMethodName(commonListenerClass, "send", packetClass), packetClass);
        }
        ServerCommonPacketListenerImpl$send.invoke(connection, packet);
    }

    public static void directTeleport(Player player, Location location) throws ReflectiveOperationException {
        final Object serverPlayer = getHandle(player);
        final Object connection = getConnection(serverPlayer);
        internalTeleport(connection, location);
    }

    public static void sendCameraPacket(Player player, Entity target) throws ReflectiveOperationException {
        final Object serverPlayer = getHandle(player);
        final Object connection = getConnection(serverPlayer);

        final Object minecraftEntity = getHandle(target);
        final Object cameraPacket = constructCameraPacket(minecraftEntity);

        sendPacket(connection, cameraPacket);
    }
}
