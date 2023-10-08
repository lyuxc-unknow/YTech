package com.yanny.ytech.network.kinetic;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.network.generic.NetworkUtils;
import com.yanny.ytech.network.generic.server.ServerNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KineticServerNetwork extends ServerNetwork<KineticServerNetwork, IKineticBlockEntity> {
    private static final String TAG_PROVIDERS = "providers";
    private static final String TAG_CONSUMERS = "consumers";
    private static final String TAG_DIR_PROVIDERS = "dirProviders";
    private static final String TAG_STRESS_CAPACITY = "stressCapacity";
    private static final String TAG_STRESS = "stress";
    private static final String TAG_BLOCK_POS = "pos";
    private static final String TAG_DIRECTION = "direction";
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull private final HashMap<BlockPos, Integer> providers = new HashMap<>();
    @NotNull private final HashMap<BlockPos, Integer> consumers = new HashMap<>();
    @NotNull private final Set<BlockPos> directionProviders = new HashSet<>();
    @NotNull private RotationDirection rotationDirection = RotationDirection.NONE;
    private int stressCapacity;
    private int stress;

    public KineticServerNetwork(@NotNull CompoundTag tag, int networkId, @NotNull Consumer<Integer> onChange, @NotNull Consumer<Integer> onRemove) {
        super(networkId, onChange, onRemove);
        load(tag);
    }

    public KineticServerNetwork(int networkId, @NotNull Consumer<Integer> onChange, @NotNull Consumer<Integer> onRemove) {
        super(networkId, onChange, onRemove);
    }

    @Override
    public String toString() {
        return MessageFormat.format("Id:{0,number}, C:{1,number}, P:{2,number}, K: {3, number} {4,number}/{5,number}, {6}",
                getNetworkId(), consumers.size(), providers.size(), directionProviders.size(), stress, stressCapacity, rotationDirection);
    }

    @Override
    protected boolean canAttach(@NotNull IKineticBlockEntity entity) {
        RotationDirection entityRotationDirection = entity.getRotationDirection();

        // if there is no rotation provider, or changed rotation for only our only provider
        if (directionProviders.isEmpty() || (directionProviders.size() == 1 && directionProviders.contains(entity.getBlockPos()))) {
            return true;
        }

        return entityRotationDirection == RotationDirection.NONE || rotationDirection == RotationDirection.NONE || entityRotationDirection == rotationDirection;
    }

    @Override
    protected boolean canAttach(@NotNull KineticServerNetwork network) {
        return rotationDirection == RotationDirection.NONE || network.rotationDirection == RotationDirection.NONE || rotationDirection == network.rotationDirection;
    }

    @Override
    protected void load(@NotNull CompoundTag tag) {
        if (tag.contains(TAG_PROVIDERS) && tag.getTagType(TAG_PROVIDERS) != 0) {
            tag.getList(TAG_PROVIDERS, ListTag.TAG_COMPOUND).forEach((t) ->
                    providers.put(NetworkUtils.loadBlockPos(((CompoundTag) t).getCompound(TAG_BLOCK_POS)), ((CompoundTag) t).getInt(TAG_STRESS)));
        }
        if (tag.contains(TAG_CONSUMERS) && tag.getTagType(TAG_CONSUMERS) != 0) {
            tag.getList(TAG_CONSUMERS, ListTag.TAG_COMPOUND).forEach((t) ->
                    consumers.put(NetworkUtils.loadBlockPos(((CompoundTag) t).getCompound(TAG_BLOCK_POS)), ((CompoundTag) t).getInt(TAG_STRESS)));
        }
        if (tag.contains(TAG_DIR_PROVIDERS) && tag.getTagType(TAG_DIR_PROVIDERS) != 0) {
            tag.getList(TAG_DIR_PROVIDERS, ListTag.TAG_COMPOUND).forEach((t) -> directionProviders.add(NetworkUtils.loadBlockPos((CompoundTag) t)));
        }
        if (tag.contains(TAG_STRESS_CAPACITY) && tag.getTagType(TAG_STRESS_CAPACITY) != 0) {
            stressCapacity = tag.getInt(TAG_STRESS_CAPACITY);
        }
        if (tag.contains(TAG_STRESS) && tag.getTagType(TAG_STRESS) != 0) {
            stress = tag.getInt(TAG_STRESS);
        }
        if (tag.contains(TAG_DIRECTION) && tag.getTagType(TAG_DIRECTION) != 0) {
            try {
                rotationDirection = RotationDirection.valueOf(tag.getString(TAG_DIRECTION));
            } catch (Exception ignored) {}
        }

        LOGGER.debug("Network {}: Loaded {} providers, {} consumers, {} dirProviders, {}", getNetworkId(), providers.size(),
                consumers.size(), directionProviders.size(), rotationDirection);
    }

    @Override
    protected @NotNull CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag providersTag = new ListTag();
        ListTag consumersTag = new ListTag();
        ListTag dirProvidersTag = new ListTag();

        providers.forEach((pos, stress) -> {
            CompoundTag t = new CompoundTag();
            t.put(TAG_BLOCK_POS, NetworkUtils.saveBlockPos(pos));
            t.putInt(TAG_STRESS, stress);
            providersTag.add(t);
        });
        consumers.forEach((pos, stress) -> {
            CompoundTag t = new CompoundTag();
            t.put(TAG_BLOCK_POS, NetworkUtils.saveBlockPos(pos));
            t.putInt(TAG_STRESS, stress);
            consumersTag.add(t);
        });
        directionProviders.forEach((pos) -> dirProvidersTag.add(NetworkUtils.saveBlockPos(pos)));
        tag.put(TAG_PROVIDERS, providersTag);
        tag.put(TAG_CONSUMERS, consumersTag);
        tag.put(TAG_DIR_PROVIDERS, dirProvidersTag);
        tag.putInt(TAG_STRESS_CAPACITY, stressCapacity);
        tag.putInt(TAG_STRESS, stress);
        tag.putString(TAG_DIRECTION, rotationDirection.name());
        return tag;
    }

    @Override
    protected void addAll(@NotNull KineticServerNetwork network, @NotNull Level level) {
        if (network.rotationDirection != RotationDirection.NONE && rotationDirection != RotationDirection.NONE && rotationDirection != network.rotationDirection) {
            throw new IllegalStateException("Invalid rotation direction provided!");
        }

        network.providers.forEach((pos, value) -> {
            providers.put(pos, value);
            stressCapacity += value;

            if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kineticBlockEntity) {
                kineticBlockEntity.setNetworkId(getNetworkId());
            }
        });
        network.consumers.forEach((pos, value) -> {
            consumers.put(pos, value);
            stress += value;

            if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kineticBlockEntity) {
                kineticBlockEntity.setNetworkId(getNetworkId());
            }
        });

        directionProviders.addAll(network.directionProviders);
        rotationDirection = network.rotationDirection;
    }

    @Override
    protected boolean update(@NotNull IKineticBlockEntity entity) {
        RotationDirection entityRotationDirection = entity.getRotationDirection();
        BlockPos blockPos = entity.getBlockPos();
        boolean wasChange = false;
        int value = entity.getStress();

        switch (entity.getNetworkType()) {
            case PROVIDER -> {
                int oldCapacity = stressCapacity;

                stressCapacity = stressCapacity - providers.get(blockPos) + value;
                providers.put(blockPos, value);

                if (oldCapacity != stressCapacity) {
                    wasChange = true;
                }
            }
            case CONSUMER -> {
                int oldStress = stress;

                stress = stress - providers.get(blockPos) + value;
                consumers.put(blockPos, value);

                if (oldStress != stress) {
                    wasChange = true;
                }
            }
        }

        if (directionProviders.remove(entity.getBlockPos())) {
            if (directionProviders.isEmpty()) {
                rotationDirection = RotationDirection.NONE;
            }

            wasChange = true;
        }

        if (entityRotationDirection != RotationDirection.NONE) {
            if (rotationDirection != RotationDirection.NONE && rotationDirection != entityRotationDirection) {
                throw new IllegalStateException("Invalid rotation direction provided!");
            }

            directionProviders.add(blockPos);
            rotationDirection = entityRotationDirection;
            wasChange = true;
        }

        return wasChange;
    }

    @Override
    @NotNull
    protected List<KineticServerNetwork> remove(@NotNull Function<Integer, List<Integer>> idsGetter, @NotNull Consumer<Integer> onRemove, @NotNull IKineticBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        Map<BlockPos, Integer> providerBlocks = new HashMap<>(providers);
        Map<BlockPos, Integer> consumerBlocks = new HashMap<>(consumers);
        BlockPos blockPos = blockEntity.getBlockPos();

        // remove splitting block
        providerBlocks.remove(blockPos);
        consumerBlocks.remove(blockPos);
        remove(blockEntity);

        List<BlockPos> neighbors = blockEntity.getValidNeighbors().stream().filter(pos -> providerBlocks.containsKey(pos) || consumerBlocks.containsKey(pos)).collect(Collectors.toList());

        if ((neighbors.size() == 1) || (providerBlocks.isEmpty() && consumerBlocks.isEmpty()) || level == null) { // if we are not splitting
            return List.of();
        }

        List<Integer> ids = idsGetter.apply(neighbors.size() - 1);
        BlockPos neighbor = neighbors.remove(0); // remove first network (will be our network)

        clear();
        insertConnectedPositions(this, providerBlocks, consumerBlocks, neighbor, level); // re-insert blocks

        return neighbors.stream().map((pos) -> {
            if ((providerBlocks.isEmpty() && consumerBlocks.isEmpty()) || (!providerBlocks.containsKey(pos) && !consumerBlocks.containsKey(pos))) {
                return null;
            }

            KineticServerNetwork network = new KineticServerNetwork(ids.remove(0), onChange, onRemove);
            insertConnectedPositions(network, providerBlocks, consumerBlocks, pos, level);
            return network;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    protected boolean isNotEmpty() {
        return !providers.isEmpty() || !consumers.isEmpty();
    }

    @Override
    protected boolean isValidPosition(@NotNull IKineticBlockEntity blockEntity, @NotNull BlockPos pos) {
        if (providers.containsKey(pos) || consumers.containsKey(pos)) {
            Level level = blockEntity.getLevel();

            if (level != null && level.isLoaded(pos)) {
                if (level.getBlockEntity(pos) instanceof IKineticBlockEntity IKineticBlockEntity) {
                    return IKineticBlockEntity.getValidNeighbors().contains(blockEntity.getBlockPos());
                }
            }
        }

        return false;
    }

    @Override
    protected void add(@NotNull IKineticBlockEntity entity) {
        super.add(entity);

        switch (entity.getNetworkType()) {
            case CONSUMER -> addConsumer(entity);
            case PROVIDER -> addProvider(entity);
        }
    }

    @Override
    protected void remove(@NotNull IKineticBlockEntity entity) {
        switch (entity.getNetworkType()) {
            case CONSUMER -> removeConsumer(entity);
            case PROVIDER -> removeProvider(entity);
        }

        super.remove(entity);
    }

    public int getStress() {
        return stress;
    }

    public int getStressCapacity() {
        return stressCapacity;
    }

    @NotNull
    public RotationDirection getRotationDirection() {
        return rotationDirection;
    }

    private void addProvider(@NotNull IKineticBlockEntity entity) {
        RotationDirection entityRotationDirection = entity.getRotationDirection();
        BlockPos blockPos = entity.getBlockPos();
        int stressValue = entity.getStress();

        providers.put(blockPos, stressValue);
        stressCapacity += stressValue;

        if (entityRotationDirection != RotationDirection.NONE) {
            if (rotationDirection != RotationDirection.NONE && rotationDirection != entityRotationDirection) {
                throw new IllegalStateException("Invalid rotation direction provided!");
            }

            directionProviders.add(blockPos);
            rotationDirection = entityRotationDirection;
        }
    }

    private void addConsumer(@NotNull IKineticBlockEntity entity) {
        RotationDirection entityRotationDirection = entity.getRotationDirection();
        BlockPos blockPos = entity.getBlockPos();
        int stressValue = entity.getStress();

        consumers.put(blockPos, stressValue);
        stress += stressValue;

        if (entityRotationDirection != RotationDirection.NONE) {
            if (rotationDirection != RotationDirection.NONE && rotationDirection != entityRotationDirection) {
                throw new IllegalStateException("Invalid rotation direction provided!");
            }

            directionProviders.add(blockPos);
            rotationDirection = entityRotationDirection;
        }
    }


    private void removeProvider(@NotNull IKineticBlockEntity entity) {
        BlockPos blockPos = entity.getBlockPos();
        int value = providers.remove(blockPos);

        entity.setNetworkId(-1);
        stressCapacity -= value;

        if (directionProviders.remove(blockPos) && directionProviders.isEmpty()) {
            rotationDirection = RotationDirection.NONE;
        }
    }

    private void removeConsumer(@NotNull IKineticBlockEntity entity) {
        BlockPos blockPos = entity.getBlockPos();
        int value = consumers.remove(blockPos);

        entity.setNetworkId(-1);
        stress -= value;

        if (directionProviders.remove(blockPos) && directionProviders.isEmpty()) {
            rotationDirection = RotationDirection.NONE;
        }
    }

    private void clear() {
        consumers.clear();
        providers.clear();
        directionProviders.clear();
        stressCapacity = 0;
        stress = 0;
        rotationDirection = RotationDirection.NONE;
    }

    private static void insertConnectedPositions(@NotNull KineticServerNetwork network, @NotNull Map<BlockPos, Integer> providerBlocks,
                                                 @NotNull Map<BlockPos, Integer> consumerBlocks, @NotNull BlockPos from, @NotNull Level level) {
        BlockEntity blockEntity = level.getBlockEntity(from);

        if (blockEntity instanceof IKineticBlockEntity block) {
            network.add(block);
            providerBlocks.remove(from);
            consumerBlocks.remove(from);
            block.getValidNeighbors().forEach((pos) -> {
                if (providerBlocks.containsKey(pos) || consumerBlocks.containsKey(pos)) {
                    insertConnectedPositions(network, providerBlocks, consumerBlocks, pos, level);
                }
            });
        }
    }
}
