package com.yanny.ytech.network.kinetic.block;

import com.yanny.ytech.network.kinetic.block_entity.WaterWheelBlockEntity;
import com.yanny.ytech.network.kinetic.common.IKineticBlockEntity;
import com.yanny.ytech.network.kinetic.common.KineticBlockType;
import com.yanny.ytech.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class WaterWheelBlock extends KineticBlock {
    private static final VoxelShape SHAPE_EAST_WEST = Shapes.box(0, 6/16.0, 6/16.0, 1, 10/16.0, 10/16.0);
    private static final VoxelShape SHAPE_NORTH_SOUTH = Shapes.box(6/16.0, 6/16.0, 0, 10/16.0, 10/16.0, 1);

    public WaterWheelBlock() {
        super(Properties.of());
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext blockPlaceContext) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos) {
        return true;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        return new WaterWheelBlockEntity(Registration.REGISTRATION_HOLDER.kineticNetwork().get(KineticBlockType.WATER_WHEEL).entityType().get(), pos, blockState);
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos, @NotNull CollisionContext collisionContext) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        return (direction == Direction.EAST || direction == Direction.WEST) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);

        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kineticBlockEntity) {
                kineticBlockEntity.onChangedState(state, state);
            }
        }
    }
}
