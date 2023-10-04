package com.yanny.ytech.network.generic.server;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.network.generic.common.AbstractNetwork;
import com.yanny.ytech.network.generic.common.INetworkBlockEntity;
import com.yanny.ytech.network.generic.common.NetworkFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerLevel<T extends AbstractNetwork<T, O>, O extends INetworkBlockEntity> extends SavedData {
    protected static final String TAG_NETWORKS = "networks";
    protected static final String TAG_NETWORK = "network";
    protected static final String TAG_NETWORK_ID = "networkId";
    protected static final Logger LOGGER = LogUtils.getLogger();

    @NotNull private final ConcurrentHashMap<Integer, T> networkMap = new ConcurrentHashMap<>();
    @NotNull private final SimpleChannel channel;
    @NotNull private final NetworkFactory<T, O> networkFactory;
    @NotNull private final String networkName;

    ServerLevel(@NotNull CompoundTag tag, @NotNull SimpleChannel channel, @NotNull NetworkFactory<T, O> networkFactory, @NotNull String networkName) {
        this.networkName = networkName;
        this.channel = channel;
        this.networkFactory = networkFactory;
        load(tag);
    }

    ServerLevel(@NotNull SimpleChannel channel, @NotNull NetworkFactory<T, O> networkFactory, @NotNull String networkName) {
        this.channel = channel;
        this.networkFactory = networkFactory;
        this.networkName = networkName;
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        AtomicInteger index = new AtomicInteger();

        networkMap.forEach((networkId, network) -> {
            CompoundTag itemHolder = new CompoundTag();
            itemHolder.putInt(TAG_NETWORK_ID, networkId);
            itemHolder.put(TAG_NETWORK, network.save());
            list.add(index.getAndIncrement(), itemHolder);
        });
        tag.put(TAG_NETWORKS, list);
        return tag;
    }

    public void add(@NotNull O blockEntity) {
        final int networkId = blockEntity.getNetworkId();
        T resultNetwork;
        Level level = blockEntity.getLevel();

        if (level == null) {
            return;
        }

        if (networkId >= 0) {
            resultNetwork = networkMap.get(networkId);

            if (!resultNetwork.canAttach(blockEntity)) {
                LOGGER.warn("[{}] Can't attach block {} to network at {}", networkName, blockEntity, blockEntity.getBlockPos());
                level.destroyBlock(blockEntity.getBlockPos(), true);
                return;
            }
        } else {
            //TODO possible performance improvement: first check network area intersection
            List<T> networks = networkMap.values().stream().filter((n) -> n.canConnect(blockEntity)).toList();

            if (networks.isEmpty()) {
                T network = networkFactory.createNetwork(getUniqueId(), this::onRemove);
                networkMap.put(network.getNetworkId(), network);
                resultNetwork = network;
            } else if (networks.size() == 1) {
                resultNetwork = networks.get(0);

                if (!resultNetwork.canAttach(blockEntity)) {
                    LOGGER.warn("[{}] Can't attach block {} to network at {}", networkName, blockEntity, blockEntity.getBlockPos());
                    level.destroyBlock(blockEntity.getBlockPos(), true);
                    return;
                }
            } else {
                ArrayList<T> distinctNetworks = networks.stream().filter(distinctByKey(T::getNetworkId)).collect(Collectors.toCollection(ArrayList::new));

                if (distinctNetworks.size() == 1) {
                    resultNetwork = distinctNetworks.get(0);

                    if (!resultNetwork.canAttach(blockEntity)) {
                        LOGGER.warn("[{}] Can't attach block {} to network at {}", networkName, blockEntity, blockEntity.getBlockPos());
                        level.destroyBlock(blockEntity.getBlockPos(), true);
                        return;
                    }
                } else {
                    T network = distinctNetworks.remove(0);

                    if (!network.canAttach(blockEntity) || !distinctNetworks.stream().allMatch((n) -> n.canAttach(blockEntity) && n.canAttach(network))) {
                        LOGGER.warn("[{}] Can't attach block {} to network at {}", networkName, blockEntity, blockEntity.getBlockPos());
                        level.destroyBlock(blockEntity.getBlockPos(), true);
                        return;
                    }

                    do {
                        T toRemove = distinctNetworks.remove(0);
                        network.addAll(toRemove, level);
                        networkMap.remove(toRemove.getNetworkId());
                        channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkRemoveMessage(toRemove.getNetworkId()));
                    } while (!distinctNetworks.isEmpty());

                    resultNetwork = network;
                }
            }
        }

        resultNetwork.add(blockEntity);
        setDirty();
        channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkAddedOrUpdatedMessage(resultNetwork));
    }

    public void update(@NotNull O blockEntity) {
        T network = getNetwork(blockEntity);
        Level level = blockEntity.getLevel();

        if (level == null) {
            return;
        }

        if (network != null) {
            if (network.canAttach(blockEntity)) {
                if (network.update(blockEntity)) {
                    setDirty();
                    channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkAddedOrUpdatedMessage(network));
                }
            } else {
                List<T> networks = network.remove(this::getUniqueIds, this::onRemove, blockEntity, channel);
                networkMap.putAll(networks.stream().collect(Collectors.toMap(AbstractNetwork::getNetworkId, n -> n)));
                level.destroyBlock(blockEntity.getBlockPos(), true);
                LOGGER.warn("[{}] Removed block {} from network at {}", networkName, blockEntity, blockEntity.getBlockPos());
                setDirty();

                if (network.isNotEmpty()) {
                    channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkAddedOrUpdatedMessage(network));
                }
            }
        } else {
            LOGGER.warn("[{}] UPDATE: Can't get network for block {} at {}", networkName, blockEntity, blockEntity.getBlockPos());
        }
    }

    public void remove(@NotNull O blockEntity) {
        T network = getNetwork(blockEntity);

        if (network != null) {
            List<T> networks = network.remove(this::getUniqueIds, this::onRemove, blockEntity, channel);
            networkMap.putAll(networks.stream().collect(Collectors.toMap(AbstractNetwork::getNetworkId, n -> n)));
            setDirty();

            if (network.isNotEmpty()) {
                channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkAddedOrUpdatedMessage(network));
            }
        } else {
            LOGGER.warn("[{}] REMOVE: Can't get network for block {} at {}", networkName, blockEntity, blockEntity.getBlockPos());
        }
    }

    @NotNull
    public Map<Integer, T> getNetworks() {
        return networkMap;
    }

    public T getNetwork(@NotNull O blockEntity) {
        return networkMap.get(blockEntity.getNetworkId());
    }

    private void onRemove(int networkId) {
        networkMap.remove(networkId);
        channel.send(PacketDistributor.ALL.noArg(), networkFactory.createNetworkRemoveMessage(networkId));
    }

    private int getUniqueId() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (!networkMap.containsKey(i)) {
                return i;
            }
        }

        LOGGER.error("[{}] Network keys overflow!", networkName);
        throw new IllegalStateException("Can't generate new ID for network!");
    }

    private List<Integer> getUniqueIds(int count) {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (!networkMap.containsKey(i)) {
                result.add(i);

                if (result.size() == count) {
                    return result;
                }
            }
        }

        LOGGER.error("[{}] Network keys overflow!", networkName);
        throw new IllegalStateException("Can't generate new ID for network!");
    }

    private void load(@NotNull CompoundTag tag) {
        if (tag.contains(TAG_NETWORKS)) {
            ListTag list = tag.getList(TAG_NETWORKS, CompoundTag.TAG_COMPOUND);

            list.forEach((listItem) -> {
                CompoundTag itemHolder = (CompoundTag) listItem;
                int networkId = itemHolder.getInt(TAG_NETWORK_ID);
                networkMap.put(networkId, networkFactory.createNetwork(itemHolder.getCompound(TAG_NETWORK), networkId, this::onRemove));
            });

            LOGGER.info("[{}] Loaded {} networks", networkName, networkMap.size());
        } else {
            LOGGER.info("[{}] No network loaded", networkName);
        }
    }

    private static <T> Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        return t -> ConcurrentHashMap.newKeySet().add(keyExtractor.apply(t));
    }

    /**
     * FIXME REMOVE - Testing
     */
    @Override
    public void setDirty() {
        super.setDirty();
        LOGGER.warn("[{}] Updated network. Total: {}", networkName, networkMap.size());
        networkMap.forEach((i, n) -> LOGGER.info("[{}]: {}", networkName, n.toString()));
    }
}
