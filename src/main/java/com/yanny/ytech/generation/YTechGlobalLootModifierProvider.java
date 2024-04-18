package com.yanny.ytech.generation;

import com.yanny.ytech.YTechMod;
import com.yanny.ytech.loot_modifier.AddItemModifier;
import com.yanny.ytech.loot_modifier.ReplaceItemModifier;
import com.yanny.ytech.registration.YTechItemTags;
import com.yanny.ytech.registration.YTechItems;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class YTechGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public YTechGlobalLootModifierProvider(PackOutput output) {
        super(output, YTechMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("grass_drops_fibers", new AddItemModifier(
                new LootItemCondition[] {
                        LootItemRandomChanceCondition.randomChance(0.1f).build(),
                        MatchTool.toolMatches(ItemPredicate.Builder.item().of(YTechItemTags.SHARP_FLINTS)).build(),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build()
                },
                YTechItems.GRASS_FIBERS.get()
        ));
        add("gravel_drops_pebble", new AddItemModifier(
                new LootItemCondition[]{
                        LootItemRandomChanceCondition.randomChance(0.2f).build(),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRAVEL).build()
                },
                YTechItems.PEBBLE.get()
        ));

        addReplaceLeatherByRawHide(EntityType.COW);
        addReplaceLeatherByRawHide(EntityType.HORSE);
        addReplaceLeatherByRawHide(EntityType.DONKEY);
        addReplaceLeatherByRawHide(EntityType.LLAMA);
        addReplaceLeatherByRawHide(EntityType.MULE);
        addReplaceLeatherByRawHide(EntityType.MOOSHROOM);
        addReplaceLeatherByRawHide(EntityType.TRADER_LLAMA);
        addReplaceLeatherByRawHide(EntityType.HOGLIN);
    }

    private void addReplaceLeatherByRawHide(EntityType<?> entityType) {
        add(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).getPath() + "_replace_leather_by_raw_hide", new ReplaceItemModifier(
                new LootItemCondition[] {
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityType)).build()).build()
                },
                Items.LEATHER,
                YTechItems.RAW_HIDE.get()
        ));
    }
}
