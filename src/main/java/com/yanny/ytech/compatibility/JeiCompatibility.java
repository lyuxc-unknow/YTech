package com.yanny.ytech.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ytech.compatibility.jei.*;
import com.yanny.ytech.configuration.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    protected static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DryingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TanningRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MillingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SmeltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BlockHitRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            ClientLevel level = Minecraft.getInstance().level;

            registration.addRecipes(DryingRecipeCategory.RECIPE_TYPE, DryingRecipeCategory.getRecipes(level.getRecipeManager()));
            registration.addRecipes(TanningRecipeCategory.RECIPE_TYPE, TanningRecipeCategory.getRecipes(level.getRecipeManager()));
            registration.addRecipes(MillingRecipeCategory.RECIPE_TYPE, MillingRecipeCategory.getRecipes(level.getRecipeManager()));
            registration.addRecipes(SmeltingRecipeCategory.RECIPE_TYPE, SmeltingRecipeCategory.getRecipes(level.getRecipeManager()));
            registration.addRecipes(BlockHitRecipeCategory.RECIPE_TYPE, BlockHitRecipeCategory.getRecipes(level.getRecipeManager()));
        } else {
            LOGGER.warn("JEI integration was not loaded! Level is null!");
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        DryingRecipeCategory.registerCatalyst(registration);
        TanningRecipeCategory.registerCatalyst(registration);
        MillingRecipeCategory.registerCatalyst(registration);
        SmeltingRecipeCategory.registerCatalyst(registration);
        BlockHitRecipeCategory.registerCatalyst(registration);
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return Utils.modLoc("jei_plugin");
    }
}
