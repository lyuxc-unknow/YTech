package com.yanny.ytech;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.configuration.recipe.BlockHitRecipe;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = YTechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusSubscriber {
    @NotNull private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onLevelLoad(@NotNull LevelEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();

        if (levelAccessor instanceof ServerLevel level) {
            YTechMod.KINETIC_PROPAGATOR.server().onLevelLoad(level);
            YTechMod.IRRIGATION_PROPAGATOR.server().onLevelLoad(level);
        } else if (levelAccessor instanceof ClientLevel level) {
            YTechMod.KINETIC_PROPAGATOR.client().onLevelLoad(level);
            YTechMod.IRRIGATION_PROPAGATOR.client().onLevelLoad(level);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(@NotNull LevelEvent.Unload event) {
        LevelAccessor levelAccessor = event.getLevel();

        if (levelAccessor instanceof ServerLevel level) {
            YTechMod.KINETIC_PROPAGATOR.server().onLevelUnload(level);
            YTechMod.IRRIGATION_PROPAGATOR.server().onLevelUnload(level);
        } else if (levelAccessor instanceof ClientLevel level) {
            YTechMod.KINETIC_PROPAGATOR.client().onLevelUnload(level);
            YTechMod.IRRIGATION_PROPAGATOR.client().onLevelUnload(level);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(@NotNull ServerStartingEvent event) {
        if (YTechMod.CONFIGURATION.shouldRequireValidTool()) {
            YTechMod.CONFIGURATION.getBlocksRequiringValidTool().forEach((block) ->
                    setBlockRequireValidTool(Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(block))));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        YTechMod.KINETIC_PROPAGATOR.server().onPlayerLogIn(event.getEntity());
        YTechMod.IRRIGATION_PROPAGATOR.server().onPlayerLogIn(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelTick(@NotNull TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
            if (YTechMod.CONFIGURATION.shouldRainingFillAqueduct() && event.level.isRaining() && event.level.getGameTime() % YTechMod.CONFIGURATION.getRainingFillPerNthTick() == 0) {
                YTechMod.IRRIGATION_PROPAGATOR.server().getNetworks(event.level).values().forEach((network) -> {
                    FluidStack fluidStack = new FluidStack(Fluids.WATER, YTechMod.CONFIGURATION.getRainingFillAmount() * network.storageBlockCount());
                    network.getFluidHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(@NotNull PlayerInteractEvent.LeftClickBlock event) {
        if (YTechMod.CONFIGURATION.enableCraftingSharpFlint()) {
            Player player = event.getEntity();
            Level level = event.getLevel();
            ItemStack heldItem = player.getMainHandItem();
            BlockState blockState = level.getBlockState(event.getPos());
            Direction direction = event.getFace();

            if (!level.isClientSide && !player.isCreative() && direction != null && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START && event.getHand() == InteractionHand.MAIN_HAND) {
                level.getRecipeManager().getRecipeFor(BlockHitRecipe.RECIPE_TYPE, new SimpleContainer(heldItem, blockState.getBlock().asItem().getDefaultInstance()), level).ifPresent((recipe) -> {
                    Block.popResourceFromFace(level, event.getPos(), direction, recipe.result().copy());
                    heldItem.shrink(1);
                });
            }
        }
    }

    private static void setBlockRequireValidTool(@NotNull Block block) {
        try {
            BlockState blockState = ObfuscationReflectionHelper.getPrivateValue(Block.class, block, "f_49786_"); // defaultBlockState

            if (blockState != null) {
                ObfuscationReflectionHelper.setPrivateValue(BlockBehaviour.BlockStateBase.class, blockState, Boolean.TRUE, "f_60600_"); // requiresCorrectToolForDrops
            }

            LOGGER.info("Set requiresCorrectToolForDrops on {}", block.getName());
        } catch (Exception e) {
            LOGGER.warn("Unable to set requiresCorrectToolForDrops on block " + block.getName().getString() + ": " + e.getMessage());
        }
    }
}
