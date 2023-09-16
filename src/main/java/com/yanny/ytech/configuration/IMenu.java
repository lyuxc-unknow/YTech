package com.yanny.ytech.configuration;

import com.yanny.ytech.configuration.container.Utils;
import com.yanny.ytech.registration.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.NotNull;

public interface IMenu {
    AbstractContainerMenu getContainerMenu(@NotNull Holder holder, int windowId, @NotNull Inventory inv, @NotNull BlockPos data,
                                           @NotNull MachineItemStackHandler itemStackHandler, @NotNull ContainerData containerData);

    default AbstractContainerMenu getContainerMenu(@NotNull Holder holder, int windowId, @NotNull Inventory inv, @NotNull BlockPos pos) {
        return getContainerMenu(holder, windowId, inv, pos, Utils.getMachineBlockEntity(inv.player, pos).createItemStackHandler(),
                new SimpleContainerData(Utils.getMachineBlockEntity(inv.player, pos).getDataSize()));
    }
}
