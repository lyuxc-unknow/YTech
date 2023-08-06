package com.yanny.ytech.registration;

import com.yanny.ytech.configuration.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Holder {
    @NotNull public final String key;
    @NotNull public final String name;

    Holder(@NotNull String key, @NotNull String name) {
        this.key = key;
        this.name = name;
    }

    public static class SimpleItemHolder extends Holder {
        @NotNull public final SimpleItemType object;
        @NotNull public final RegistryObject<Item> item;

        SimpleItemHolder(@NotNull SimpleItemType object, @NotNull RegistryObject<Item> item) {
            super(object.key, object.name);
            this.object = object;
            this.item = item;
        }
    }

    public static class SimpleToolHolder extends Holder {
        @NotNull public final SimpleToolType object;
        @NotNull public final RegistryObject<Item> item;

        SimpleToolHolder(@NotNull SimpleToolType object, @NotNull RegistryObject<Item> item) {
            super(object.key, object.name);
            this.object = object;
            this.item = item;
        }
    }

    public static class MaterialHolder<T, U extends ConfigLoader.BaseObject<T>> extends Holder {
        @NotNull public final U object;
        @NotNull public final ConfigLoader.MaterialHolder materialHolder;

        MaterialHolder(@NotNull U object, @NotNull ConfigLoader.MaterialHolder materialHolder) {
            super(
                    Objects.requireNonNull(object.name.getKey(materialHolder.material), "Key must be non null"),
                    Objects.requireNonNull(object.name.getLocalized(materialHolder.material), "Name must be non null")
            );
            this.object = Objects.requireNonNull(object, "Product must be non null");
            this.materialHolder = Objects.requireNonNull(materialHolder, "Material must be non null");
        }
    }

    public static class ItemHolder extends MaterialHolder<ItemObjectType, ConfigLoader.ItemObject> {
        @NotNull public final RegistryObject<Item> item;

        public ItemHolder(@NotNull ConfigLoader.ItemObject product, @NotNull ConfigLoader.MaterialHolder materialHolder, @NotNull RegistryObject<Item> item) {
            super(product, materialHolder);
            this.item = item;
        }
    }

    public static class BlockHolder extends MaterialHolder<BlockObjectType, ConfigLoader.BlockObject> {
        @NotNull public final RegistryObject<Block> block;

        public BlockHolder(@NotNull ConfigLoader.BlockObject product, @NotNull ConfigLoader.MaterialHolder materialHolder, @NotNull RegistryObject<Block> block) {
            super(product, materialHolder);
            this.block = block;
        }
    }

    public static class FluidHolder extends MaterialHolder<FluidObjectType, ConfigLoader.FluidObject> {
        @NotNull public final RegistryObject<Block> block;
        @NotNull public final RegistryObject<FluidType> type;
        @NotNull public final RegistryObject<Fluid> source;
        @NotNull public final RegistryObject<Fluid> flowing;
        @NotNull public final RegistryObject<Item> bucket;

        public FluidHolder(@NotNull ConfigLoader.FluidObject product, @NotNull ConfigLoader.MaterialHolder materialHolder,
                           @NotNull RegistryObject<Block> block, @NotNull RegistryObject<FluidType> type, @NotNull RegistryObject<Fluid> source,
                           @NotNull RegistryObject<Fluid> flowing, @NotNull RegistryObject<Item> bucket) {
            super(product, materialHolder);
            this.block = block;
            this.type = type;
            this.source = source;
            this.flowing = flowing;
            this.bucket = bucket;
        }
    }

    public static class ToolHolder extends MaterialHolder<ToolObjectType, ConfigLoader.ToolObject> {
        @NotNull public final RegistryObject<Item> tool;

        ToolHolder(@NotNull ConfigLoader.ToolObject object, @NotNull ConfigLoader.MaterialHolder materialHolder, @NotNull RegistryObject<Item> tool) {
            super(object, materialHolder);
            this.tool = tool;
        }
    }
}
