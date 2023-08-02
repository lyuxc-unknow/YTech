package com.yanny.ytech.machine.block;

import com.yanny.ytech.configuration.ConfigLoader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

class CrusherBlock extends MachineBlock {
    public CrusherBlock(Supplier<BlockEntityType<? extends BlockEntity>> entityType, ConfigLoader.Machine machine, ConfigLoader.Tier tier) {
        super(entityType, machine, tier);
    }
}
