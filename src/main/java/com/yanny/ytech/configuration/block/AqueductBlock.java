package com.yanny.ytech.configuration.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yanny.ytech.configuration.SimpleBlockType;
import com.yanny.ytech.configuration.TextureHolder;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.block_entity.AqueductBlockEntity;
import com.yanny.ytech.registration.Holder;
import com.yanny.ytech.registration.Registration;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class AqueductBlock extends IrrigationBlock {
    private static final VoxelShape SHAPE_BOTTOM = Shapes.box(0, 0, 0, 1, 2/16.0, 1);
    private static final VoxelShape SHAPE_NORTH_SIDE = Shapes.box(0, 0, 0, 1, 1, 2/16.0);
    private static final VoxelShape SHAPE_EAST_SIDE = Shapes.box(14/16.0, 0, 0, 1, 1, 1);
    private static final VoxelShape SHAPE_SOUTH_SIDE = Shapes.box(0, 0, 14/16.0, 1, 1, 1);
    private static final VoxelShape SHAPE_WEST_SIDE = Shapes.box(0, 0, 0, 2/16.0, 1, 1);

    private static final BooleanProperty NORTH_EAST = BooleanProperty.create("north_east");
    private static final BooleanProperty NORTH_WEST = BooleanProperty.create("north_west");
    private static final BooleanProperty SOUTH_EAST = BooleanProperty.create("south_east");
    private static final BooleanProperty SOUTH_WEST = BooleanProperty.create("south_west");

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (enumMap) -> {
        enumMap.put(Direction.NORTH, NORTH);
        enumMap.put(Direction.EAST, EAST);
        enumMap.put(Direction.SOUTH, SOUTH);
        enumMap.put(Direction.WEST, WEST);
    }));
    public static final Map<Direction, Integer> ANGLE_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (enumMap) -> {
        enumMap.put(Direction.NORTH, 0);
        enumMap.put(Direction.EAST, 90);
        enumMap.put(Direction.SOUTH, 180);
        enumMap.put(Direction.WEST, 270);
    }));

    public AqueductBlock(@NotNull Holder.SimpleBlockHolder holder) {
        super(Properties.copy(Blocks.TERRACOTTA));
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = SHAPE_BOTTOM;

        if (state.getValue(NORTH)) {
            shape = Shapes.join(shape, SHAPE_NORTH_SIDE, BooleanOp.OR);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.join(shape, SHAPE_EAST_SIDE, BooleanOp.OR);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.join(shape, SHAPE_SOUTH_SIDE, BooleanOp.OR);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.join(shape, SHAPE_WEST_SIDE, BooleanOp.OR);
        }

        return shape;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(EAST).add(WEST).add(SOUTH).add(NORTH).add(NORTH_EAST).add(NORTH_WEST).add(SOUTH_EAST).add(SOUTH_WEST);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext blockPlaceContext) {
        BlockState state = defaultBlockState();
        Level level = blockPlaceContext.getLevel();
        BlockPos pos = blockPlaceContext.getClickedPos();
        boolean hasNorthConnection = level.getBlockState(pos.north()).getBlock() instanceof IrrigationBlock;
        boolean hasEastConnection = level.getBlockState(pos.east()).getBlock() instanceof IrrigationBlock;
        boolean hasSouthConnection = level.getBlockState(pos.south()).getBlock() instanceof IrrigationBlock;
        boolean hasWestConnection = level.getBlockState(pos.west()).getBlock() instanceof IrrigationBlock;

        state = state.setValue(NORTH, !hasNorthConnection);
        state = state.setValue(EAST, !hasEastConnection);
        state = state.setValue(SOUTH, !hasSouthConnection);
        state = state.setValue(WEST, !hasWestConnection);

        state = state.setValue(NORTH_WEST, hasSide(level, pos.west(), NORTH) || hasSide(level, pos.north(), WEST) || !(hasWestConnection && hasNorthConnection));
        state = state.setValue(NORTH_EAST, hasSide(level, pos.north(), EAST) || hasSide(level, pos.east(), NORTH) || !(hasNorthConnection && hasEastConnection));
        state = state.setValue(SOUTH_EAST, hasSide(level, pos.east(), SOUTH) || hasSide(level, pos.south(), EAST) || !(hasEastConnection && hasSouthConnection));
        state = state.setValue(SOUTH_WEST, hasSide(level, pos.south(), WEST) || hasSide(level, pos.west(), SOUTH) || !(hasSouthConnection && hasWestConnection));

        return state;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                  @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        boolean hasNorthConnection = level.getBlockState(pos.north()).getBlock() instanceof IrrigationBlock;
        boolean hasEastConnection = level.getBlockState(pos.east()).getBlock() instanceof IrrigationBlock;
        boolean hasSouthConnection = level.getBlockState(pos.south()).getBlock() instanceof IrrigationBlock;
        boolean hasWestConnection = level.getBlockState(pos.west()).getBlock() instanceof IrrigationBlock;

        state = state.setValue(NORTH, !hasNorthConnection);
        state = state.setValue(EAST, !hasEastConnection);
        state = state.setValue(SOUTH, !hasSouthConnection);
        state = state.setValue(WEST, !hasWestConnection);

        state = state.setValue(NORTH_WEST, hasSide(level, pos.west(), NORTH) || hasSide(level, pos.north(), WEST) || !(hasWestConnection && hasNorthConnection));
        state = state.setValue(NORTH_EAST, hasSide(level, pos.north(), EAST) || hasSide(level, pos.east(), NORTH) || !(hasNorthConnection && hasEastConnection));
        state = state.setValue(SOUTH_EAST, hasSide(level, pos.east(), SOUTH) || hasSide(level, pos.south(), EAST) || !(hasEastConnection && hasSouthConnection));
        state = state.setValue(SOUTH_WEST, hasSide(level, pos.south(), WEST) || hasSide(level, pos.west(), SOUTH) || !(hasSouthConnection && hasWestConnection));

        return state;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        Holder.SimpleBlockHolder blockHolder = Registration.HOLDER.simpleBlocks().get(SimpleBlockType.AQUEDUCT);

        if (blockHolder instanceof Holder.EntitySimpleBlockHolder holder) {
            return new AqueductBlockEntity(holder.entityType.get(), pos, blockState);
        } else {
            throw new IllegalStateException("Invalid holder type!");
        }
    }

    @NotNull
    public static TextureHolder[] getTexture() {
        return List.of(new TextureHolder(-1, -1, Utils.modBlockLoc("terracotta_bricks"))).toArray(TextureHolder[]::new);
    }

    public static void registerModel(@NotNull Holder.SimpleBlockHolder holder, @NotNull BlockStateProvider provider) {
        ResourceLocation[] textures = holder.object.getTextures();
        ModelFile base = provider.models().getBuilder(holder.key)
                .parent(provider.models().getExistingFile(Utils.mcBlockLoc("block")))
                .element().allFaces((direction, faceBuilder) -> {
                    switch(direction) {
                        case NORTH -> faceBuilder.uvs(0, 14, 16, 16).texture("#0");
                        case EAST -> faceBuilder.uvs(0, 14, 16, 16).texture("#0");
                        case SOUTH -> faceBuilder.uvs(0, 14, 16, 16).texture("#0");
                        case WEST -> faceBuilder.uvs(0, 14, 16, 16).texture("#0");
                        case UP -> faceBuilder.uvs(0, 0, 16, 16).texture("#0");
                        case DOWN -> faceBuilder.uvs(0, 0, 16, 16).texture("#0");
                    }
                })
                .from(0, 0, 0).to(16, 2, 16).end()
                .texture("particle", textures[0])
                .texture("0", textures[0]);
        ModelFile side = provider.models().getBuilder(holder.key + "_side")
                .parent(provider.models().getExistingFile(Utils.mcBlockLoc("block")))
                .element().allFaces((direction, faceBuilder) -> {
                    switch(direction) {
                        case NORTH -> faceBuilder.uvs(2, 0, 14, 14).texture("#0");
                        case EAST -> faceBuilder.uvs(14, 0, 16, 14).texture("#0");
                        case SOUTH -> faceBuilder.uvs(2, 0, 14, 14).texture("#0");
                        case WEST -> faceBuilder.uvs(0, 0, 2, 14).texture("#0");
                        case UP -> faceBuilder.uvs(2, 0, 14, 2).texture("#0");
                    }
                })
                .from(2, 2, 0).to(14, 16, 2).end()
                .texture("particle", textures[0])
                .texture("0", textures[0]);
        ModelFile edge = provider.models().getBuilder(holder.key + "_edge")
                .parent(provider.models().getExistingFile(Utils.mcBlockLoc("block")))
                .element().allFaces((direction, faceBuilder) -> {
                    switch(direction) {
                        case NORTH -> faceBuilder.uvs(14, 0, 16, 14).texture("#0");
                        case EAST -> faceBuilder.uvs(14, 0, 16, 14).texture("#0");
                        case SOUTH -> faceBuilder.uvs(0, 0, 2, 14).texture("#0");
                        case WEST -> faceBuilder.uvs(0, 0, 2, 14).texture("#0");
                        case UP -> faceBuilder.uvs(0, 0, 2, 2).texture("#0");
                    }
                })
                .from(0, 2, 0).to(2, 16, 2).end()
                .texture("particle", textures[0])
                .texture("0", textures[0]);

        MultiPartBlockStateBuilder builder = provider.getMultipartBuilder(holder.block.get()).part().modelFile(base).addModel().end();

        PROPERTY_BY_DIRECTION.forEach((dir, value) -> builder.part().modelFile(side).rotationY(ANGLE_BY_DIRECTION.get(dir)).addModel().condition(value, true).end());
        builder.part().modelFile(edge).rotationY(ANGLE_BY_DIRECTION.get(Direction.NORTH)).addModel().condition(NORTH_WEST, true).end();
        builder.part().modelFile(edge).rotationY(ANGLE_BY_DIRECTION.get(Direction.EAST)).addModel().condition(NORTH_EAST, true).end();
        builder.part().modelFile(edge).rotationY(ANGLE_BY_DIRECTION.get(Direction.SOUTH)).addModel().condition(SOUTH_EAST, true).end();
        builder.part().modelFile(edge).rotationY(ANGLE_BY_DIRECTION.get(Direction.WEST)).addModel().condition(SOUTH_WEST, true).end();
        provider.itemModels().getBuilder(holder.key).parent(base);
    }

    public static void registerRecipe(Holder.SimpleBlockHolder holder, Consumer<FinishedRecipe> recipeConsumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, holder.block.get())
                .define('#', Registration.item(SimpleBlockType.TERRACOTTA_BRICKS))
                .pattern("# #")
                .pattern("# #")
                .pattern("###")
                .unlockedBy(Utils.getHasName(), RecipeProvider.has(SimpleBlockType.TERRACOTTA_BRICKS.itemTag))
                .save(recipeConsumer, Utils.modLoc(holder.key));
    }

    private static boolean hasSide(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BooleanProperty property) {
        BlockState blockState = level.getBlockState(pos);
        boolean isIrrigation = blockState.getBlock() instanceof IrrigationBlock;
        return isIrrigation && blockState.hasProperty(property) && blockState.getValue(property);
    }
}
