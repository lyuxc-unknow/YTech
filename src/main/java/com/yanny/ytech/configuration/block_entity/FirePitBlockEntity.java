package com.yanny.ytech.configuration.block_entity;

import com.yanny.ytech.registration.YTechBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FirePitBlockEntity extends BlockEntity implements BlockEntityTicker<FirePitBlockEntity> {
    private final SimpleProgressHandler<CampfireCookingRecipe> progressHandler;

    public FirePitBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(YTechBlockEntityTypes.FIRE_PIT.get(), pPos, pBlockState);
        progressHandler = new SimpleProgressHandler<>(RecipeType.CAMPFIRE_COOKING);
    }

    public ItemStack getItem() {
        return progressHandler.getItem();
    }

    public int getProgress() {
        return progressHandler.getProgress();
    }

    public InteractionResult onUse(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                                   @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ItemStack holdingItemStack = player.getItemInHand(hand);

            if (progressHandler.isEmpty()) {
                progressHandler.setupCrafting(level, holdingItemStack, AbstractCookingRecipe::getCookingTime);
            } else {
                Block.popResourceFromFace(level, pos, hitResult.getDirection(), progressHandler.getItem());
            }

            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void tick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FirePitBlockEntity blockEntity) {
        int heatLevel = state.getValue(BlockStateProperties.LEVEL);
        Function<CampfireCookingRecipe, Boolean> canProcess = (recipe) -> state.getValue(BlockStateProperties.LIT) && heatLevel > 0;
        Function<CampfireCookingRecipe, Float> getStep = (recipe) -> heatLevel / 15f;
        BiConsumer<Container, CampfireCookingRecipe> onFinish = (container, recipe) -> {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), recipe.assemble(container, level.registryAccess()));
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
        };

        if (progressHandler.tick(level, canProcess, getStep, onFinish)) {
            setChanged(level, pos, state);
        }
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        progressHandler.load(tag);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        progressHandler.save(tag);
    }
}
