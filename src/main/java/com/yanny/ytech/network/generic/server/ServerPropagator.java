package com.yanny.ytech.network.generic.server;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.YTechMod;
import com.yanny.ytech.network.generic.NetworkUtils;
import com.yanny.ytech.network.generic.common.AbstractNetwork;
import com.yanny.ytech.network.generic.common.INetworkBlockEntity;
import com.yanny.ytech.network.generic.common.NetworkFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;

public class ServerPropagator<N extends AbstractNetwork<N, O>, O extends INetworkBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull private final HashMap<LevelAccessor, ServerLevel<N, O>> levelMap = new HashMap<>();
    @NotNull private final SimpleChannel channel;
    @NotNull private final NetworkFactory<N, O> networkFactory;
    @NotNull private final String networkName;

    public ServerPropagator(@NotNull SimpleChannel channel, @NotNull NetworkFactory<N, O> networkFactory, @NotNull String networkName) {
        this.channel = channel;
        this.networkFactory = networkFactory;
        this.networkName = networkName;
    }

    public void add(@NotNull O blockEntity) {
        levelMap.get(blockEntity.getLevel()).add(blockEntity);
    }

    public void changed(@NotNull O blockEntity) {
        levelMap.get(blockEntity.getLevel()).update(blockEntity);
    }

    public void remove(@NotNull O blockEntity) {
        levelMap.get(blockEntity.getLevel()).remove(blockEntity);
    }

    public void onLevelLoad(@NotNull net.minecraft.server.level.ServerLevel level) {
        LOGGER.debug("[{}] Preparing propagators for {}", networkName, NetworkUtils.getLevelId(level));
        levelMap.put(level, level.getDataStorage().computeIfAbsent((tag) -> new ServerLevel<>(tag, channel, networkFactory, networkName),
                () -> new ServerLevel<>(channel, networkFactory, networkName), YTechMod.MOD_ID + "_" + networkName));
        LOGGER.debug("[{}] Prepared propagators for {}", networkName, NetworkUtils.getLevelId(level));
    }

    public void onLevelUnload(@NotNull net.minecraft.server.level.ServerLevel level) {
        LOGGER.debug("[{}] Removing propagator for {}", networkName, NetworkUtils.getLevelId(level));
        levelMap.remove(level);
        LOGGER.debug("[{}] Removed propagator for {}", networkName, NetworkUtils.getLevelId(level));
    }

    public void onPlayerLogIn(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), networkFactory.createLevelSyncMessage(levelMap.get(serverPlayer.level()).getNetworks()));
        }
    }

    @Nullable
    public N getNetwork(@NotNull O blockEntity) {
        ServerLevel<N, O> level = levelMap.get(blockEntity.getLevel());

        if (level != null) {
            return level.getNetwork(blockEntity);
        } else {
            LOGGER.warn("[{}] No " + networkName + " network for level {}", networkName, blockEntity.getLevel());
            return null;
        }
    }
}
