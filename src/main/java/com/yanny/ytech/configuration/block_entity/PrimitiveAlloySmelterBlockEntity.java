package com.yanny.ytech.configuration.block_entity;

import com.yanny.ytech.configuration.MachineItemStackHandler;
import com.yanny.ytech.configuration.container.PrimitiveAlloySmelterContainerMenu;
import com.yanny.ytech.configuration.recipe.AlloyingRecipe;
import com.yanny.ytech.configuration.recipe.TwoItemsRecipeInput;
import com.yanny.ytech.registration.YTechBlockEntityTypes;
import com.yanny.ytech.registration.YTechRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrimitiveAlloySmelterBlockEntity extends AbstractPrimitiveMachineBlockEntity {
    private static final String TAG_RECIPE_INPUT_LEFT = "recipeInputLeft";
    private static final String TAG_RECIPE_INPUT_RIGHT = "recipeInputRight";
    private static final int SLOT_INPUT_LEFT = 0;
    private static final int SLOT_INPUT_RIGHT = 1;
    private static final int SLOT_FUEL = 2;
    private static final int SLOT_OUTPUT = 3;

    @Nullable private ItemStack recipeInputLeft = ItemStack.EMPTY;

    @Nullable private ItemStack recipeInputRight = ItemStack.EMPTY;

    public PrimitiveAlloySmelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(YTechBlockEntityTypes.PRIMITIVE_ALLOY_SMELTER.get(), pos, blockState, YTechRecipeTypes.ALLOYING.get());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ytech.primitive_alloy_smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
        return new PrimitiveAlloySmelterContainerMenu(windowId, inventory.player, worldPosition, itemStackHandler, containerData);
    }

    @Override
    public @NotNull MachineItemStackHandler createItemStackHandler() {
        return new MachineItemStackHandler.Builder()
                .addInputSlot(45, 16, this::canInput)
                .addInputSlot(65, 16, this::canInput)
                .addInputSlot(55, 52, (itemStackHandler, slot, itemStack) -> itemStack.getBurnTime(RecipeType.BLASTING) > 0)
                .addOutputSlot(116, 35)
                .setOnChangeListener(this::setChanged)
                .build();
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (level == null || !level.isClientSide) { // server side
            if (tag.contains(TAG_RECIPE_INPUT_LEFT)) {
                recipeInputLeft = ItemStack.parseOptional(provider, tag.getCompound(TAG_RECIPE_INPUT_LEFT));
            } else {
                recipeInputLeft = null;
            }

            if (tag.contains(TAG_RECIPE_INPUT_RIGHT)) {
                recipeInputRight = ItemStack.parseOptional(provider, tag.getCompound(TAG_RECIPE_INPUT_RIGHT));
            } else {
                recipeInputRight = null;
            }
        }
    }

    @Override
    public boolean hasActiveRecipe() {
        return recipeInputLeft != null && recipeInputRight != null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (level == null || !level.isClientSide) {
            if (recipeInputLeft != null) {
                CompoundTag itemStack = new CompoundTag();

                recipeInputLeft.save(provider, itemStack);
                tag.put(TAG_RECIPE_INPUT_LEFT, itemStack);
            }

            if (recipeInputRight != null) {
                CompoundTag itemStack = new CompoundTag();

                recipeInputRight.save(provider, itemStack);
                tag.put(TAG_RECIPE_INPUT_RIGHT, itemStack);
            }
        }
    }

    @Override
    protected boolean hasItemsInInput() {
        return !itemStackHandler.getStackInSlot(SLOT_INPUT_LEFT).isEmpty() && !itemStackHandler.getStackInSlot(SLOT_INPUT_RIGHT).isEmpty();
    }

    @Override
    protected int getFuelSlot() {
        return SLOT_FUEL;
    }

    @Override
    protected void startRecipe(@NotNull AtomicBoolean hasChanged) {
        if (level != null) {
            ItemStack inputLeft = itemStackHandler.getStackInSlot(SLOT_INPUT_LEFT);
            ItemStack inputRight = itemStackHandler.getStackInSlot(SLOT_INPUT_RIGHT);

            level.getRecipeManager().getRecipeFor(YTechRecipeTypes.ALLOYING.get(), new TwoItemsRecipeInput(inputLeft, inputRight), level).ifPresent((recipe) -> {
                ItemStack result = itemStackHandler.getStackInSlot(SLOT_OUTPUT);
                AlloyingRecipe r = recipe.value();

                if (r.minTemperature() <= temperature && (result.isEmpty()
                        || (ItemStack.isSameItemSameComponents(result, r.result()) && result.getMaxStackSize() > result.getCount() + r.result().getCount()))) {
                    if (r.matchesIngredient1(inputLeft, false) && r.matchesIngredient2(inputRight, false)) {
                        recipeInputLeft = inputLeft.split(r.getInput1Count());
                        recipeInputRight = inputRight.split(r.getInput2Count());
                    } else {
                        recipeInputLeft = inputLeft.split(r.getInput2Count());
                        recipeInputRight = inputRight.split(r.getInput1Count());
                    }

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

            level.getRecipeManager().getRecipeFor(YTechRecipeTypes.ALLOYING.get(), new TwoItemsRecipeInput(recipeInputLeft, recipeInputRight), level).ifPresent((r) -> {
                if (result.isEmpty()) {
                    itemStackHandler.setStackInSlot(SLOT_OUTPUT, r.value().result().copy());
                } else {
                    result.grow(r.value().result().getCount());
                }
            });
            recipeInputLeft = null;
            recipeInputRight = null;
        }
    }

    @Override
    protected boolean isValidRecipeInInput() {
        if (level != null) {
            ItemStack inputLeft = itemStackHandler.getStackInSlot(SLOT_INPUT_LEFT);
            ItemStack inputRight = itemStackHandler.getStackInSlot(SLOT_INPUT_RIGHT);

            return level.getRecipeManager().getAllRecipesFor(YTechRecipeTypes.ALLOYING.get()).stream()
                    .anyMatch((recipe) -> recipe.value().matchesFully(inputLeft, inputRight, false));
        }

        return false;
    }

    private boolean canInput(@NotNull MachineItemStackHandler itemStackHandler, int slot, @NotNull ItemStack itemStack) {
        ItemStack itemStack1 = slot != SLOT_INPUT_LEFT ? itemStackHandler.getStackInSlot(SLOT_INPUT_LEFT) : itemStack;
        ItemStack itemStack2 = slot != SLOT_INPUT_RIGHT ? itemStackHandler.getStackInSlot(SLOT_INPUT_RIGHT) : itemStack;
        boolean isPartial = itemStack1.isEmpty() || itemStack2.isEmpty();

        if (level != null) {
            return level.getRecipeManager().getAllRecipesFor(YTechRecipeTypes.ALLOYING.get()).stream().anyMatch((recipe) -> {
                if (isPartial) {
                    return recipe.value().matchesPartially(itemStack1, itemStack2, true);
                } else {
                    return recipe.value().matchesFully(itemStack1, itemStack2, true);
                }
            });
        } else {
            return false;
        }
    }

}
