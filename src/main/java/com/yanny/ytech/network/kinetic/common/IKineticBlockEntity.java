package com.yanny.ytech.network.kinetic.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IKineticBlockEntity {
    BlockPos getBlockPos();
    Level getLevel();
    List<BlockPos> getValidNeighbors();
    int getNetworkId();
    void setNetworkId(int id);
    void onRemove();
    void onChangedState(BlockState oldBlockState, BlockState newBlockState);
    KineticNetworkType getKineticNetworkType();
    int getStress();
    @NotNull RotationDirection getRotationDirection();
}
