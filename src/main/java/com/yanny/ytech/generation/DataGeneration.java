package com.yanny.ytech.generation;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

public class DataGeneration {
    public static void generate(@NotNull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        YTechBlockTags blockTags = new YTechBlockTags(packOutput, event.getLookupProvider(), event.getExistingFileHelper());

        generator.addProvider(event.includeClient(), new YTechBlockStates(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new YTechItemModels(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new YTechLanguages(packOutput, "en_us"));
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new YTechFluidTags(packOutput, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new YTechItemTags(packOutput, event.getLookupProvider(), blockTags.contentsGetter(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new YTechBiomeTags(packOutput, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new YTechRecipes(packOutput, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new YTechLootTables(packOutput));
        generator.addProvider(event.includeServer(), new YTechGlobalLootModifier(packOutput));
        generator.addProvider(event.includeServer(), new YTechWorldGen(packOutput, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new YTechAdvancements(packOutput, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}