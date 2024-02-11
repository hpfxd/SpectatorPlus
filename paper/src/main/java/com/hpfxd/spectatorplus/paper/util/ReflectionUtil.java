package com.hpfxd.spectatorplus.paper.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class ReflectionUtil {
    private static final ReflectionRemapper REMAPPER = ReflectionRemapper.forReobfMappingsInPaperJar();

    private static Method CraftPlayer$getHandle;
    private static Field ServerPlayer$connection;
    private static Method ServerGamePacketListenerImpl$internalTeleport;

    private ReflectionUtil() {
    }

    private static Object getServerPlayer(Player player) throws ReflectiveOperationException {
        if (CraftPlayer$getHandle == null) {
            CraftPlayer$getHandle = player.getClass().getMethod("getHandle");
        }
        return CraftPlayer$getHandle.invoke(player);
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

    public static void directTeleport(Player player, Location location) throws ReflectiveOperationException {
        final Object serverPlayer = getServerPlayer(player);
        final Object connection = getConnection(serverPlayer);
        internalTeleport(connection, location);
    }
}
