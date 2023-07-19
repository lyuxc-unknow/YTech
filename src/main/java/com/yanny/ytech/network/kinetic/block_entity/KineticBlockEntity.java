package com.yanny.ytech.network.kinetic.block_entity;

import com.yanny.ytech.YTechMod;
import com.yanny.ytech.network.kinetic.KineticUtils;
import com.yanny.ytech.network.kinetic.common.IKineticBlockEntity;
import com.yanny.ytech.network.kinetic.common.KineticNetworkType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KineticBlockEntity extends BlockEntity implements IKineticBlockEntity {
    private static final String NETWORK_ID = "networkId";

    protected int networkId = -1;
    private final List<BlockPos> validNeighbors;
    private final KineticNetworkType kineticNetworkType;
    private final int stress;

    public KineticBlockEntity(BlockEntityType<? extends BlockEntity> entityType, BlockPos pos, BlockState blockState, Direction currentDirection,
                              List<Direction> validConnections, KineticNetworkType kineticNetworkType, int stress) {
        super(entityType, pos, blockState);
        validNeighbors = KineticUtils.getDirections(validConnections, pos, currentDirection);
        this.kineticNetworkType = kineticNetworkType;
        this.stress = stress;
    }

    @Override
    public List<BlockPos> getValidNeighbors() {
        return validNeighbors;
    }

    @Override
    public int getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(int networkId) {
        this.networkId = networkId;
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public KineticNetworkType getKineticNetworkType() {
        return kineticNetworkType;
    }

    @Override
    public int getStress() {
        return stress;
    }

    @Override
    public void onRemove() {
        YTechMod.KINETIC_PROPAGATOR.server().remove(this);
        setChanged();
    }

    @Override
    public void onChangedState(BlockState oldBlockState, BlockState newBlockState) {
        //TODO handle rotation etc (or not handle power change)
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains(NETWORK_ID)) {
            networkId = tag.getInt(NETWORK_ID);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && !level.isClientSide) {
            if (networkId < 0) {
                YTechMod.KINETIC_PROPAGATOR.server().add(this);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                setChanged();
            }
        }
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(NETWORK_ID, networkId);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        networkId = tag.getInt(NETWORK_ID);
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(NETWORK_ID, networkId);
    }
}
