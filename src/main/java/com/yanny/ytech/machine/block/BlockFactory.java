package com.yanny.ytech.machine.block;

import com.yanny.ytech.configuration.ConfigLoader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockFactory {
    public static MachineBlock create(Supplier<BlockEntityType<? extends BlockEntity>> entityTypeSupplier, ConfigLoader.Machine machine, ConfigLoader.Tier tier) {
        return switch (machine.id()) {
            case FURNACE -> new FurnaceBlock(entityTypeSupplier, machine, tier);
            case CRUSHER -> switch (tier.id()) {
                case STONE -> new KineticMachineBlock(entityTypeSupplier, machine, tier);
                case STEAM -> new CrusherBlock(entityTypeSupplier, machine, tier);
            };
        };
    }
}
