package com.yanny.ytech.network.kinetic.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ytech.YTechMod;
import com.yanny.ytech.network.kinetic.common.IKineticBlockEntity;
import com.yanny.ytech.network.kinetic.common.KineticNetwork;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class WaterWheelRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final RandomSource randomSource = RandomSource.create(42);

    public WaterWheelRenderer(BlockEntityRendererProvider.Context context) {
        super();
        blockRenderDispatcher = context.getBlockRenderDispatcher();
    }
    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel bakedmodel = blockRenderDispatcher.getBlockModel(blockState);
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Level level = blockEntity.getLevel();

        poseStack.pushPose();
        poseStack.rotateAround(facing.getRotation(), 0.5f, 0.5f, 0.5f);

        if (level != null) {
            if (blockEntity instanceof IKineticBlockEntity kineticBlock) {
                KineticNetwork network = YTechMod.KINETIC_PROPAGATOR.client().getNetwork(kineticBlock);

                if (network != null && network.getStress() > 0) {
                    poseStack.rotateAround(facing.getRotation().rotationX((level.getGameTime() + partialTick) / (float) network.getStress()), 0.5f, 0.5f, 0.5f);
                }
            }
        }

        for (RenderType rt : bakedmodel.getRenderTypes(blockState, randomSource, blockEntity.getModelData())) {
            blockRenderDispatcher.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), blockState,
                    bakedmodel, 0, 0, 0, combinedLight, combinedOverlay, blockEntity.getModelData(), rt);
        }


        poseStack.popPose();
    }
}
