package com.yanny.ytech.configuration.block_entity;

import com.yanny.ytech.configuration.MachineItemStackHandler;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.container.StrainerContainerMenu;
import com.yanny.ytech.registration.YTechBlockEntityTypes;
import com.yanny.ytech.registration.YTechItemTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StrainerBlockEntity extends MachineBlockEntity {
    public StrainerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(YTechBlockEntityTypes.STRAINER.get(), pPos, pBlockState);
    }

    public boolean hasMesh() {
        return !itemStackHandler.getStackInSlot(0).isEmpty();
    }

    public void onRandomTick(ServerLevel serverLevel, BlockPos pos) {
        MinecraftServer server = serverLevel.getServer();
        LootTable table = server.getLootData().getLootTable(Utils.modLoc("fishing/strainer"));
        ItemStack mesh = itemStackHandler.getStackInSlot(0);
        ObjectArrayList<ItemStack> randomItems = table.getRandomItems(new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.TOOL, itemStackHandler.getStackInSlot(0))
                .create(LootContextParamSets.FISHING)
        );

        if (mesh.hurt(1, serverLevel.random, null)) {
            mesh.shrink(1);
            mesh.setDamageValue(0);
        }

        itemStackHandler.setStackInSlot(0, mesh);
        itemStackHandler.outputOperation(() -> randomItems.forEach((itemStack) -> ItemHandlerHelper.insertItemStacked(itemStackHandler, itemStack, false)));
    }

    @NotNull
    @Override
    public MachineItemStackHandler createItemStackHandler() {
        MachineItemStackHandler.Builder builder = new MachineItemStackHandler.Builder()
                .addInputSlot(26, 34, (handler, slot, stack) -> stack.is(YTechItemTags.MESHES.tag))
                .setOnChangeListener(this::setChanged);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                builder.addOutputSlot(8 + (x + 4) * 18, 16 + y * 18);
            }
        }

        return builder.build();
    }

    @NotNull
    @Override
    public ContainerData createContainerData() {
        return new SimpleContainerData(0);
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ytech.strainer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
        return new StrainerContainerMenu(windowId, inventory.player, worldPosition, itemStackHandler, containerData);
    }
}
