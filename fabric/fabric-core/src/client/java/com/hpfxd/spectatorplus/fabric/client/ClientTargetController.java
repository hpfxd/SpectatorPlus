package com.hpfxd.spectatorplus.fabric.client;

import com.hpfxd.spectatorplus.fabric.client.mixin.ClientLevelAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * Handles setting the spectator target to a client-requested entity.
 * <p>
 * This is done by sending an "entity attack" packet to the server, which the server should accept and set the spectator
 * target to the requested entity. In order for the server to accept this packet, the client must be within a 6 block
 * distance from the entity. This is ensured by sending a packet to teleport the client to the entity at the same time
 * as the attack packet.
 * <p>
 * If the entity is not currently tracked by the client (likely too far away), the client does not know the entity's ID,
 * which the attack packet needs. So in this case, the teleport packet is sent, and the client waits up to
 * {@link ClientTargetController#WAITING_LIMIT_TICKS} ticks for the entity to start being tracked. When the entity is
 * tracked, the client then sends an attack packet after another teleport packet to make sure the 6 block distance
 * requirement is met when the attack packet arrives on the server-side.
 */
public class ClientTargetController {
    public static final int WAITING_LIMIT_TICKS = 100;

    private static UUID wantedTargetId;
    private static int wantedTargetTicks;

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(ClientTargetController::tick);
        ClientEntityEvents.ENTITY_LOAD.register(ClientTargetController::onEntityTrackingStart);
    }

    private static void tick(Minecraft mc) {
        if (wantedTargetId != null && ++wantedTargetTicks >= WAITING_LIMIT_TICKS) {
            wantedTargetId = null;
        }
    }

    private static void onEntityTrackingStart(Entity entity, ClientLevel level) {
        if (wantedTargetId != null && wantedTargetId.equals(entity.getUUID())) {
            wantedTargetId = null;
            startSpectatingTrackedEntity(Minecraft.getInstance(), entity);
        }
    }

    private static void startSpectatingTrackedEntity(Minecraft mc, Entity entity) {
        // Send teleport to entity packet just before we send the attack packet, just to make sure we're not further
        // than the 6 block interaction limit by the time the server receives it.
        mc.getConnection().send(new ServerboundTeleportToEntityPacket(entity.getUUID()));
        mc.gameMode.attack(mc.player, entity);
    }

    public static void requestTargetFromServer(Minecraft mc, UUID entityUuid) {
        final Entity trackedEntity = ((ClientLevelAccessor) mc.level).invokeGetEntities().get(entityUuid);
        if (trackedEntity == null) {
            mc.getConnection().send(new ServerboundTeleportToEntityPacket(entityUuid));
            wantedTargetId = entityUuid;
            wantedTargetTicks = 0;
        } else {
            startSpectatingTrackedEntity(mc, trackedEntity);
        }
    }
}
