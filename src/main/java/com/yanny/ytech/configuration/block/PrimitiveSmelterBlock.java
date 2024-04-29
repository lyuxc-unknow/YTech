package com.yanny.ytech.configuration.block;

import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.block_entity.PrimitiveSmelterBlockEntity;
import com.yanny.ytech.configuration.recipe.RemainingShapedRecipe;
import com.yanny.ytech.registration.YTechBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

public class PrimitiveSmelterBlock extends AbstractPrimitiveMachineBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState pState) {
        return new PrimitiveSmelterBlockEntity(pos, pState);
    }

    public static void registerModel(@NotNull BlockStateProvider provider) {
        ResourceLocation casing = Utils.modBlockLoc("bricks");
        ResourceLocation top = Utils.modBlockLoc("machine/primitive_smelter_top");
        ResourceLocation face = Utils.modBlockLoc("machine/primitive_smelter_front");
        ResourceLocation facePowered = Utils.modBlockLoc("machine/primitive_smelter_front_powered");
        BlockModelBuilder model = provider.models().cube(Utils.getPath(YTechBlocks.PRIMITIVE_SMELTER), casing, top, face, casing, casing, casing).texture("particle", casing);
        BlockModelBuilder modelPowered = provider.models().cube(Utils.getPath(YTechBlocks.PRIMITIVE_SMELTER) + "_powered", casing, top, facePowered, casing, casing, casing).texture("particle", casing);

        provider.getVariantBuilder(YTechBlocks.PRIMITIVE_SMELTER.get())
                .partialState().with(HORIZONTAL_FACING, Direction.NORTH).with(POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).build())
                .partialState().with(HORIZONTAL_FACING, Direction.EAST).with(POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(90).build())
                .partialState().with(HORIZONTAL_FACING, Direction.SOUTH).with(POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(180).build())
                .partialState().with(HORIZONTAL_FACING, Direction.WEST).with(POWERED, false).setModels(ConfiguredModel.builder().modelFile(model).rotationY(270).build())
                .partialState().with(HORIZONTAL_FACING, Direction.NORTH).with(POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).build())
                .partialState().with(HORIZONTAL_FACING, Direction.EAST).with(POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(90).build())
                .partialState().with(HORIZONTAL_FACING, Direction.SOUTH).with(POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(180).build())
                .partialState().with(HORIZONTAL_FACING, Direction.WEST).with(POWERED, true).setModels(ConfiguredModel.builder().modelFile(modelPowered).rotationY(270).build());
        provider.itemModels().getBuilder(Utils.getPath(YTechBlocks.PRIMITIVE_SMELTER)).parent(model);
    }

    public static void registerRecipe(@NotNull RecipeOutput recipeConsumer) {
        RemainingShapedRecipe.Builder.shaped(RecipeCategory.MISC, YTechBlocks.PRIMITIVE_SMELTER.get())
                .define('#', Items.FURNACE)
                .define('B', Items.BRICKS)
                .pattern("BBB")
                .pattern("B#B")
                .pattern("BBB")
                .unlockedBy(RecipeProvider.getHasName(Items.BRICKS), RecipeProvider.has(Items.BRICKS))
                .save(recipeConsumer, Utils.modLoc(YTechBlocks.PRIMITIVE_SMELTER));
    }
}
