package com.yanny.ytech.generation;

import com.yanny.ytech.GeneralUtils;
import com.yanny.ytech.YTechMod;
import com.yanny.ytech.configuration.MaterialType;
import com.yanny.ytech.registration.YTechItemTags;
import com.yanny.ytech.registration.YTechItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class YTechItemTagsProvider extends ItemTagsProvider {
    public YTechItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagLookup<Block>> tagLookup, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, tagLookup, YTechMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        tag(YTechItemTags.ANTLERS).add(YTechItems.ANTLER.get());
        tag(YTechItemTags.BASKETS).add(YTechItems.BASKET.get());
        tag(YTechItemTags.BREAD_DOUGHS).add(YTechItems.BREAD_DOUGH.get());
        tag(YTechItemTags.BRICK_MOLDS).add(YTechItems.BRICK_MOLD.get());
        tag(YTechItemTags.CLAY_BUCKETS).add(YTechItems.CLAY_BUCKET.get());
        tag(YTechItemTags.COOKED_VENISON).add(YTechItems.COOKED_VENISON.get());
        tag(YTechItemTags.DRIED_BEEFS).add(YTechItems.DRIED_BEEF.get());
        tag(YTechItemTags.DRIED_CHICKENS).add(YTechItems.DRIED_CHICKEN.get());
        tag(YTechItemTags.DRIED_CODS).add(YTechItems.DRIED_COD.get());
        tag(YTechItemTags.DRIED_MUTTONS).add(YTechItems.DRIED_MUTTON.get());
        tag(YTechItemTags.DRIED_PORKCHOP).add(YTechItems.DRIED_PORKCHOP.get());
        tag(YTechItemTags.DRIED_RABBITS).add(YTechItems.DRIED_RABBIT.get());
        tag(YTechItemTags.DRIED_SALMONS).add(YTechItems.DRIED_SALMON.get());
        tag(YTechItemTags.DRIED_VENISON).add(YTechItems.DRIED_VENISON.get());
        tag(YTechItemTags.FLOURS).add(YTechItems.FLOUR.get());
        tag(YTechItemTags.GRASS_FIBERS).add(YTechItems.GRASS_FIBERS.get());
        tag(YTechItemTags.GRASS_TWINES).add(YTechItems.GRASS_TWINE.get());
        tag(YTechItemTags.IRON_BLOOMS).add(YTechItems.IRON_BLOOM.get());
        tag(YTechItemTags.LAVA_BUCKETS).add(Items.LAVA_BUCKET, YTechItems.LAVA_CLAY_BUCKET.get());
        tag(YTechItemTags.LEATHER_STRIPS).add(YTechItems.LEATHER_STRIPS.get());
        tag(YTechItemTags.PEBBLES).add(YTechItems.PEBBLE.get());
        tag(YTechItemTags.RAW_HIDES).add(YTechItems.RAW_HIDE.get());
        tag(YTechItemTags.SHARP_FLINTS).add(YTechItems.SHARP_FLINT.get());
        tag(YTechItemTags.UNFIRED_BRICKS).add(YTechItems.UNFIRED_BRICK.get());
        tag(YTechItemTags.UNFIRED_CLAY_BUCKETS).add(YTechItems.UNFIRED_CLAY_BUCKET.get());
        tag(YTechItemTags.VENISON).add(YTechItems.VENISON.get());
        tag(YTechItemTags.WATER_BUCKETS).add(Items.WATER_BUCKET, YTechItems.WATER_CLAY_BUCKET.get());

        tag(YTechItemTags.AQUEDUCTS).add(YTechItems.AQUEDUCT.get());
        tag(YTechItemTags.AQUEDUCT_FERTILIZERS).add(YTechItems.AQUEDUCT_FERTILIZER.get());
        tag(YTechItemTags.AQUEDUCT_HYDRATORS).add(YTechItems.AQUEDUCT_HYDRATOR.get());
        tag(YTechItemTags.AQUEDUCT_VALVES).add(YTechItems.AQUEDUCT_VALVE.get());
        tag(YTechItemTags.BRICK_CHIMNEYS).add(YTechItems.BRICK_CHIMNEY.get());
        tag(YTechItemTags.BRONZE_ANVILS).add(YTechItems.BRONZE_ANVIL.get());
        tag(YTechItemTags.FIRE_PITS).add(YTechItems.FIRE_PIT.get());
        tag(YTechItemTags.GRASS_BEDS).add(YTechItems.GRASS_BED.get());
        tag(YTechItemTags.MILLSTONES).add(YTechItems.MILLSTONE.get());
        tag(YTechItemTags.PRIMITIVE_ALLOY_SMELTERS).add(YTechItems.PRIMITIVE_ALLOY_SMELTER.get());
        tag(YTechItemTags.PRIMITIVE_SMELTERS).add(YTechItems.PRIMITIVE_SMELTER.get());
        tag(YTechItemTags.REINFORCED_BRICKS).add(YTechItems.REINFORCED_BRICKS.get());
        tag(YTechItemTags.REINFORCED_BRICK_CHIMNEYS).add(YTechItems.REINFORCED_BRICK_CHIMNEY.get());
        tag(YTechItemTags.TERRACOTTA_BRICKS).add(YTechItems.TERRACOTTA_BRICKS.get());
        tag(YTechItemTags.TERRACOTTA_BRICK_SLABS).add(YTechItems.TERRACOTTA_BRICK_SLAB.get());
        tag(YTechItemTags.TERRACOTTA_BRICK_STAIRS).add(YTechItems.TERRACOTTA_BRICK_STAIRS.get());
        tag(YTechItemTags.THATCH).add(YTechItems.THATCH.get());
        tag(YTechItemTags.THATCH_SLABS).add(YTechItems.THATCH_SLAB.get());
        tag(YTechItemTags.THATCH_STAIRS).add(YTechItems.THATCH_STAIRS.get());

        tag(YTechItemTags.FERTILIZER).add(Items.BONE_MEAL);
        tag(YTechItemTags.DEER_FOOD).add(Items.APPLE, Items.CARROT, Items.WHEAT);

        materialTag(YTechItems.ARROWS, YTechItemTags.ARROWS);
        materialTag(YTechItems.AXES, YTechItemTags.AXES, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.BOLTS, YTechItemTags.BOLTS);
        materialTag(YTechItems.BOOTS, YTechItemTags.BOOTS, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.CHESTPLATES, YTechItemTags.CHESTPLATES, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.CRUSHED_MATERIALS, YTechItemTags.CRUSHED_MATERIALS);
        materialTag(YTechItems.FILES, YTechItemTags.FILES);
        materialTag(YTechItems.HAMMERS, YTechItemTags.HAMMERS);
        materialTag(YTechItems.HELMETS, YTechItemTags.HELMETS, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.HOES, YTechItemTags.HOES, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.INGOTS, YTechItemTags.INGOTS, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.KNIVES, YTechItemTags.KNIVES);
        materialTag(YTechItems.LEGGINGS, YTechItemTags.LEGGINGS, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.MORTAR_AND_PESTLES, YTechItemTags.MORTAR_AND_PESTLES);
        materialTag(YTechItems.PICKAXES, YTechItemTags.PICKAXES, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.PLATES, YTechItemTags.PLATES);
        materialTag(YTechItems.RAW_MATERIALS, YTechItemTags.RAW_MATERIALS, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.RODS, YTechItemTags.RODS);
        materialTag(YTechItems.SAWS, YTechItemTags.SAWS);
        materialTag(YTechItems.SAW_BLADES, YTechItemTags.SAW_BLADES);
        materialTag(YTechItems.SHOVELS, YTechItemTags.SHOVELS, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));
        materialTag(YTechItems.SPEARS, YTechItemTags.SPEARS);
        materialTag(YTechItems.SWORDS, YTechItemTags.SWORDS, EnumSet.of(MaterialType.GOLD, MaterialType.IRON));

        materialOreTag(YTechItems.DEEPSLATE_ORES, YTechItemTags.DEEPSLATE_ORES, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.DRYING_RACKS, YTechItemTags.DRYING_RACKS);
        materialTag(YTechItems.GRAVEL_DEPOSITS, YTechItemTags.GRAVEL_DEPOSITS);
        materialOreTag(YTechItems.NETHER_ORES, YTechItemTags.NETHER_ORES, EnumSet.of(MaterialType.GOLD));
        materialTag(YTechItems.RAW_STORAGE_BLOCKS, YTechItemTags.RAW_STORAGE_BLOCKS, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.SAND_DEPOSITS, YTechItemTags.SAND_DEPOSITS);
        materialOreTag(YTechItems.STONE_ORES, YTechItemTags.STONE_ORES, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.STORAGE_BLOCKS, YTechItemTags.STORAGE_BLOCKS, MaterialType.VANILLA_METALS);
        materialTag(YTechItems.TANNING_RACKS, YTechItemTags.TANNING_RACKS);

        tag(ItemTags.ANVIL).add(YTechItems.BRONZE_ANVIL.get());
        tag(ItemTags.BEDS).add(YTechItems.GRASS_BED.get());
        tag(Tags.Items.ORES_IN_GROUND_DEEPSLATE).add(filteredMaterials(YTechItems.DEEPSLATE_ORES, MaterialType.VANILLA_METALS));
        tag(Tags.Items.ORES_IN_GROUND_NETHERRACK).add(filteredMaterials(YTechItems.NETHER_ORES, EnumSet.of(MaterialType.GOLD)));
        tag(Tags.Items.ORES_IN_GROUND_STONE).add(filteredMaterials(YTechItems.STONE_ORES, MaterialType.VANILLA_METALS));
        tag(ItemTags.SLABS).add(YTechItems.TERRACOTTA_BRICK_SLAB.get(), YTechItems.THATCH_SLAB.get());
        tag(ItemTags.STAIRS).add(YTechItems.TERRACOTTA_BRICK_STAIRS.get(), YTechItems.THATCH_STAIRS.get());
    }

    private void materialTag(YTechItems.MaterialItem materialItem, YTechItemTags.MaterialTag materialTag) {
        GeneralUtils.sortedStreamSet(materialItem.materials(), Comparator.comparing(t -> t.key)).forEach((material) -> {
            tag(materialTag.of(material)).add(materialItem.of(material).get());
            tag(materialTag.tag).addTag(materialTag.of(material));
        });
    }

    private void materialTag(YTechItems.MaterialItem materialItem, YTechItemTags.MaterialTag materialTag, EnumSet<MaterialType> excludeMaterials) {
        GeneralUtils.sortedStreamSet(materialItem.materials(), Comparator.comparing(t -> t.key)).forEach((material) -> {
            if (!excludeMaterials.contains(material)) {
                tag(materialTag.of(material)).add(materialItem.of(material).get());
                tag(materialTag.tag).addTag(materialTag.of(material));
            }
        });
    }

    private void materialOreTag(YTechItems.MaterialItem materialItem, YTechItemTags.MaterialTag materialTag, EnumSet<MaterialType> excludeMaterials) {
        GeneralUtils.sortedStreamSet(materialItem.entries(), Comparator.comparing(t -> t.getKey().key)).forEach((entry) -> {
            MaterialType material = entry.getKey();

            if (!excludeMaterials.contains(material)) {
                DeferredItem<Item> item = entry.getValue();

                tag(materialTag.of(material)).add(item.get());
                tag(materialTag.tag).add(item.get());

                switch (material) {
                    case IRON -> tag(ItemTags.IRON_ORES).add(item.get().asItem());
                    case COPPER -> tag(ItemTags.COPPER_ORES).add(item.get().asItem());
                    case GOLD -> tag(ItemTags.GOLD_ORES).add(item.get().asItem());
                }
            }
        });
    }

    private static Item[] filteredMaterials(YTechItems.MaterialItem item, EnumSet<MaterialType> exclude) {
        return item.entries().stream().filter((entry) -> !exclude.contains(entry.getKey())).sorted(Comparator.comparing(t -> t.getKey().key)).map(Map.Entry::getValue).map(DeferredItem::get).toArray(Item[]::new);
    }
}
