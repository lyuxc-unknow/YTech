package com.yanny.ytech.generation;

import com.yanny.ytech.YTechMod;
import com.yanny.ytech.configuration.YTechConfigLoader;
import com.yanny.ytech.registration.Registration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

class YTechBlockStates extends BlockStateProvider {
    private static final ResourceLocation ORE_OVERLAY = Utils.getBlockTexture("ore_overlay");
    private static final ResourceLocation RAW_STORAGE_BLOCK = Utils.getBlockTexture("raw_storage_block");
    private static final ResourceLocation STORAGE_BLOCK = Utils.getBlockTexture("storage_block");

    public YTechBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, YTechMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Registration.REGISTRATION_HOLDER.ore().forEach((material, stoneMap) -> stoneMap.forEach(this::registerOre));
        Registration.REGISTRATION_HOLDER.rawStorageBlock().forEach((material, registry) -> registerTintedCube(registry, RAW_STORAGE_BLOCK));
        Registration.REGISTRATION_HOLDER.storageBlock().forEach((material, registry) -> registerTintedCube(registry, STORAGE_BLOCK));
        Registration.REGISTRATION_HOLDER.machine().forEach((machine, tierMap) -> tierMap.forEach((tier, holder) -> registerMachine(holder.block(), machine, tier)));
    }

    private void registerOre(Block stone, @NotNull RegistryObject<Block> registry) {
        String path = registry.getId().getPath();
        BlockModelBuilder model = models().cubeAll(path, Utils.getBaseBlockTexture(stone));

        model.renderType(mcLoc("cutout"));
        model.texture("overlay", ORE_OVERLAY);
        model.element().allFaces(((direction, faceBuilder) -> faceBuilder.texture("#all").cullface(direction)));
        model.element().allFaces(((direction, faceBuilder) -> faceBuilder.texture("#overlay").tintindex(0).cullface(direction)));
        getVariantBuilder(registry.get()).forAllStates((state) -> ConfiguredModel.builder().modelFile(model).build());
        // create item model from block
        itemModels().getBuilder(path).parent(model);
    }

    private void registerTintedCube(@NotNull RegistryObject<Block> registry, @NotNull ResourceLocation texture) {
        String path = registry.getId().getPath();
        BlockModelBuilder model = models().cubeAll(path, texture);

        model.element().allFaces((direction, faceBuilder) -> faceBuilder.texture("#all").tintindex(0).cullface(direction));
        getVariantBuilder(registry.get()).forAllStates((state) -> ConfiguredModel.builder().modelFile(model).build());
        // create item model from block
        itemModels().getBuilder(path).parent(model);
    }

    private void registerMachine(RegistryObject<Block> registry, YTechConfigLoader.Machine machine, YTechConfigLoader.Tier tier) {
        String path = registry.getId().getPath();
        ResourceLocation casing = Utils.getBlockTexture("casing/" + tier.id());
        ResourceLocation face = Utils.getBlockTexture("machine/" + tier.id() + "_" + machine.id());
        ResourceLocation facePowered = Utils.getBlockTexture("machine/" + tier.id() + "_" + machine.id() + "_powered");
        BlockModelBuilder model = models().orientableWithBottom(path, casing, face, casing, casing);
        BlockModelBuilder modelPowered = models().orientableWithBottom(path + "_powered", casing, facePowered, casing, casing);

        model.element().allFaces((direction, faceBuilder) -> {
            switch (direction) {
                case UP -> faceBuilder.texture("#top").uvs(0, 0, 8, 16).cullface(direction);
                case DOWN -> faceBuilder.texture("#bottom").uvs(0, 0, 8, 16).cullface(direction);
                case SOUTH, WEST, EAST -> faceBuilder.texture("#side").uvs(8, 0, 16, 16).cullface(direction);
                case NORTH -> faceBuilder.texture("#front").cullface(direction);
            }
        });
        modelPowered.element().allFaces((direction, faceBuilder) -> {
            switch (direction) {
                case UP -> faceBuilder.texture("#top").uvs(0, 0, 8, 16).cullface(direction);
                case DOWN -> faceBuilder.texture("#bottom").uvs(0, 0, 8, 16).cullface(direction);
                case SOUTH, WEST, EAST -> faceBuilder.texture("#side").uvs(8, 0, 16, 16).cullface(direction);
                case NORTH -> faceBuilder.texture("#front").cullface(direction);
            }
        });
        getVariantBuilder(registry.get())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(BlockStateProperties.POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST).with(BlockStateProperties.POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(90).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).with(BlockStateProperties.POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(180).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST).with(BlockStateProperties.POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(270).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(BlockStateProperties.POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST).with(BlockStateProperties.POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(90).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).with(BlockStateProperties.POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(180).build())
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST).with(BlockStateProperties.POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(270).build())
        ;
        // create item model from block
        itemModels().getBuilder(path).parent(model);
    }
}
