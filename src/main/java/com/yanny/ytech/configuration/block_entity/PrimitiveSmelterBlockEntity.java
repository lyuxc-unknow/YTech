package com.yanny.ytech.configuration.block_entity;

import com.yanny.ytech.configuration.MachineItemStackHandler;
import com.yanny.ytech.configuration.recipe.SmeltingRecipe;
import com.yanny.ytech.registration.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrimitiveSmelterBlockEntity extends AbstractPrimitiveMachineBlockEntity {
    private static final String TAG_RECIPE_INPUT = "recipeInput";
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_OUTPUT = 2;

    @Nullable private ItemStack recipeInput = ItemStack.EMPTY;

    public PrimitiveSmelterBlockEntity(Holder holder, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(holder, blockEntityType, pos, blockState);
    }

    @NotNull
    @Override
    public MachineItemStackHandler createItemStackHandler() {
        return new MachineItemStackHandler.Builder()
                .addInputSlot(55, 16, this::canInput)
                .addInputSlot(55, 52, (itemStackHandler, slot, itemStack) -> CommonHooks.getBurnTime(itemStack, RecipeType.BLASTING) > 0)
                .addOutputSlot(116, 35)
                .setOnChangeListener(this::setChanged)
                .build();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (level == null || !level.isClientSide) { // server side
            if (tag.contains(TAG_RECIPE_INPUT)) {
                recipeInput = ItemStack.of(tag.getCompound(TAG_RECIPE_INPUT));
            } else {
                recipeInput = null;
            }
        }
    }

    @Override
    public boolean hasActiveRecipe() {
        return recipeInput != null;
    }

    @Nullable
    public ItemStack processingItem() {
        return recipeInput;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        if (level == null || !level.isClientSide) {
            if (recipeInput != null) {
                CompoundTag itemStack = new CompoundTag();

                recipeInput.save(itemStack);
                tag.put(TAG_RECIPE_INPUT, itemStack);
            }
        }
    }

    @Override
    protected boolean hasItemsInInput() {
        return !itemStackHandler.getStackInSlot(SLOT_INPUT).isEmpty();
    }

    @Override
    protected int getFuelSlot() {
        return SLOT_FUEL;
    }

    @Override
    protected void startRecipe(@NotNull AtomicBoolean hasChanged) {
        if (level != null) {
            ItemStack input = itemStackHandler.getStackInSlot(SLOT_INPUT);

            level.getRecipeManager().getRecipeFor(SmeltingRecipe.RECIPE_TYPE, new SimpleContainer(input), level).ifPresent((recipe) -> {
                ItemStack result = itemStackHandler.getStackInSlot(SLOT_OUTPUT);
                SmeltingRecipe r = recipe.value();

                if (r.minTemperature() <= temperature && (result.isEmpty() || (ItemStack.isSameItemSameTags(result, r.result()) && result.getMaxStackSize() > result.getCount()))) {
                    recipeInput = input.split(1);
                    leftSmelting = smeltingTime = r.smeltingTime();
                    recipeTemperature = r.minTemperature();
                    hasChanged.set(true);
                }
            });
        }
    }

    @Override
    protected void finishRecipe() {
        if (level != null) {
            ItemStack result = itemStackHandler.getStackInSlot(SLOT_OUTPUT);

            level.getRecipeManager().getRecipeFor(SmeltingRecipe.RECIPE_TYPE, new SimpleContainer(recipeInput), level).ifPresent((r) -> {
                if (result.isEmpty()) {
                    itemStackHandler.setStackInSlot(SLOT_OUTPUT, r.value().result().copy());
                } else {
                    result.grow(1);
                }
            });
            recipeInput = null;
        }
    }

    @Override
    protected boolean isValidRecipeInInput() {
        if (level != null) {
            ItemStack itemStack = itemStackHandler.getStackInSlot(SLOT_INPUT);
            return level.getRecipeManager().getRecipeFor(SmeltingRecipe.RECIPE_TYPE, new SimpleContainer(itemStack), level).isPresent();
        }

        return false;
    }

    private boolean canInput(@NotNull MachineItemStackHandler itemStackHandler, int slot, @NotNull ItemStack itemStack) {
        if (level != null) {
            return level.getRecipeManager().getRecipeFor(SmeltingRecipe.RECIPE_TYPE, new SimpleContainer(itemStack), level).isPresent();
        } else {
            return false;
        }
    }
}
