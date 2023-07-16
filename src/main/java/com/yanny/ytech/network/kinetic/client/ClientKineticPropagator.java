package com.yanny.ytech.network.kinetic.client;

import com.yanny.ytech.network.kinetic.common.KineticPropagator;
import com.yanny.ytech.network.kinetic.message.LevelSyncMessage;
import com.yanny.ytech.network.kinetic.message.NetworkAddedOrUpdatedMessage;
import com.yanny.ytech.network.kinetic.message.NetworkRemovedMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ClientKineticPropagator extends KineticPropagator<ClientLevel, ClientKineticLevel> {
    private final Minecraft minecraft = Minecraft.getInstance();

    public void onLevelLoad(@NotNull ClientLevel level) {
        LOGGER.debug("Preparing rotary propagator for {}", getLevelId(level));
        levelMap.put(level, new ClientKineticLevel());
        LOGGER.debug("Prepared rotary propagator for {}", getLevelId(level));
    }

    public void onLevelUnload(@NotNull ClientLevel level) {
        LOGGER.debug("Removing rotary propagator for {}", getLevelId(level));
        levelMap.remove(level);
        LOGGER.debug("Removed rotary propagator for {}", getLevelId(level));
    }

    public void onSyncLevel(LevelSyncMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            levelMap.clear(); // client have only one instance of level
            levelMap.put(minecraft.level, new ClientKineticLevel(msg.networkMap()));
        });
        context.setPacketHandled(true);
    }

    public void onNetworkAddedOrUpdated(NetworkAddedOrUpdatedMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientKineticLevel level = levelMap.get(minecraft.level);

            if (level != null) {
                level.onNetworkAddedOrUpdated(msg.network());
                LOGGER.info("Added or updated network {}" , msg.network().getNetworkId());
            } else {
                LOGGER.warn("No level stored for {}", minecraft.level);
            }
        });
        context.setPacketHandled(true);
    }

    public void onNetworkRemoved(NetworkRemovedMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientKineticLevel level = levelMap.get(minecraft.level);

            if (level != null) {
                level.onNetworkRemoved(msg.networkId());
                LOGGER.info("Removed network {}", msg.networkId());
            } else {
                LOGGER.warn("No level stored for {}", minecraft.level);
            }
        });
        context.setPacketHandled(true);
    }
}
