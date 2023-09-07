package com.yanny.ytech.configuration.screen;

import com.yanny.ytech.configuration.container.FurnaceContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FurnaceScreen extends MachineScreen<FurnaceContainerMenu> {
    public FurnaceScreen(AbstractContainerMenu container, Inventory inventory, Component title) {
        super((FurnaceContainerMenu) container, inventory, title);
    }
}
