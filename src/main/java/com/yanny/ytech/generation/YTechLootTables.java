package com.yanny.ytech.generation;

import com.yanny.ytech.GeneralUtils;
import com.yanny.ytech.configuration.ItemObjectType;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static com.yanny.ytech.registration.Registration.HOLDER;

class YTechLootTables extends LootTableProvider {
    public YTechLootTables(PackOutput packOutput) {
        super(packOutput, Collections.emptySet(), getSubProviders());
    }

    public static List<SubProviderEntry> getSubProviders() {
        return List.of(
                new LootTableProvider.SubProviderEntry(YTechBlockLootSub::new, LootContextParamSets.BLOCK)
        );
    }

    private static class YTechBlockLootSub extends BlockLootSubProvider {
        protected YTechBlockLootSub() {
            super(new HashSet<>(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            GeneralUtils.mapToStream(HOLDER.blocks()).forEach(h -> {
                switch (h.object.id) {
                    case STONE_ORE, DEEPSLATE_ORE, NETHERRACK_ORE ->
                            add(h.block.get(), (block -> createOreDrop(block, GeneralUtils.getFromMap(HOLDER.items(),
                                    ItemObjectType.RAW_MATERIAL, h.materialHolder.material).item.get())));
                    default -> dropSelf(h.block.get());
                }
            });
            GeneralUtils.mapToStream(HOLDER.machine()).forEach(h -> dropSelf(h.block.get()));
            GeneralUtils.mapToStream(HOLDER.kineticNetwork()).forEach(h -> dropSelf(h.block.get()));
        }

        @NotNull
        @Override
        protected Iterable<Block> getKnownBlocks() {
            Stream<Block> stream = Stream.of(
                    GeneralUtils.mapToStream(HOLDER.blocks()).flatMap(e -> e.block.stream()),
                    GeneralUtils.mapToStream(HOLDER.machine()).flatMap(h -> h.block.stream()),
                    GeneralUtils.mapToStream(HOLDER.kineticNetwork()).flatMap(h -> h.block.stream())
            ).flatMap(i -> i);
            return stream.toList();
        }
    }
}
