package com.yanny.ytech;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.configuration.block.GrassBedBlock;
import com.yanny.ytech.configuration.recipe.BlockHitRecipe;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = YTechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusSubscriber {
    @NotNull private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerSpawnSet(@NotNull PlayerSetSpawnEvent event) {
        BlockPos pos = event.getNewSpawn();

        if (pos != null && event.getEntity().level().getBlockState(pos).getBlock() instanceof GrassBedBlock) {
            event.setCanceled(true);
        }
    }

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
            YTechMod.CONFIGURATION.getBlocksRequiringValidTool().forEach((tag) -> BuiltInRegistries.BLOCK.getTag(tag).ifPresent(block -> block.stream().forEach(blockHolder -> setBlockRequireValidTool(blockHolder.value()))));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        YTechMod.KINETIC_PROPAGATOR.server().onPlayerLogIn(event.getEntity());
        YTechMod.IRRIGATION_PROPAGATOR.server().onPlayerLogIn(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelTick(@NotNull TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER && event.level instanceof ServerLevel level) {
            YTechMod.IRRIGATION_PROPAGATOR.server().getNetworks(level).values().forEach((network) -> network.tick((ServerLevel) event.level));
        }

        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER && event.level instanceof ServerLevel level) {
            YTechMod.KINETIC_PROPAGATOR.server().tick(level);
            YTechMod.IRRIGATION_PROPAGATOR.server().tick(level);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(@NotNull ChunkWatchEvent.Watch event) {
        YTechMod.KINETIC_PROPAGATOR.server().onChunkWatch(event.getLevel(), event.getPlayer(), event.getChunk());
        YTechMod.IRRIGATION_PROPAGATOR.server().onChunkWatch(event.getLevel(), event.getPlayer(), event.getChunk());
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
                    Block.popResourceFromFace(level, event.getPos(), direction, recipe.value().result().copy());
                    heldItem.shrink(1);
                });
            }
        }
    }

    private static void setBlockRequireValidTool(@NotNull Block block) {
        try {
            BlockState blockState = ObfuscationReflectionHelper.getPrivateValue(Block.class, block, "defaultBlockState"); // defaultBlockState

            if (blockState != null) {
                ObfuscationReflectionHelper.setPrivateValue(BlockBehaviour.BlockStateBase.class, blockState, Boolean.TRUE, "requiresCorrectToolForDrops"); // requiresCorrectToolForDrops
            }

            LOGGER.info("Set requiresCorrectToolForDrops on {}", Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)));
        } catch (Exception e) {
            LOGGER.warn("Unable to set requiresCorrectToolForDrops on block " + Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)) + ": " + e.getMessage());
        }
    }
}
