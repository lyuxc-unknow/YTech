package com.yanny.ytech.compatibility.jei;

import com.yanny.ytech.YTechMod;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.recipe.AlloyingRecipe;
import com.yanny.ytech.registration.YTechItems;
import com.yanny.ytech.registration.YTechRecipeTypes;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AlloyingRecipeCategory implements IRecipeCategory<AlloyingRecipe> {
    public static final RecipeType<AlloyingRecipe> RECIPE_TYPE = RecipeType.create(YTechMod.MOD_ID, "alloying", AlloyingRecipe.class);

    private final Font font = Minecraft.getInstance().font;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public AlloyingRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = Utils.modLoc("textures/gui/jei.png");
        background = guiHelper.createDrawable(location, 82, 0, 92, 62);
        icon = guiHelper.createDrawableItemStack(new ItemStack(YTechItems.PRIMITIVE_ALLOY_SMELTER.get()));
        localizedName = Component.translatable("emi.category.ytech.alloying");
    }

    @NotNull
    @Override
    public RecipeType<AlloyingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return localizedName;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addItemStacks(Arrays.stream(recipe.ingredient1().getItems()).peek(i -> i.setCount(recipe.ingredient1().count())).toList());
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 5).addItemStacks(Arrays.stream(recipe.ingredient2().getItems()).peek(i -> i.setCount(recipe.ingredient2().count())).toList());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 71,  23).addItemStack(recipe.result());
    }

    @Override
    public void draw(@NotNull AlloyingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String smeltingText = (recipe.smeltingTime() / 20) + "s";
        String temperatureText = recipe.minTemperature() + "°C";

        int stringWidth = font.width(smeltingText);
        guiGraphics.drawString(font, smeltingText, getWidth() - stringWidth, getHeight() - 8, 0xFF808080, false);

        stringWidth = font.width(temperatureText);
        guiGraphics.drawString(font, temperatureText, getWidth() - stringWidth, 0, 0xFF808080, false);
    }

    public static List<AlloyingRecipe> getRecipes(@NotNull RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(YTechRecipeTypes.ALLOYING.get()).stream().map(RecipeHolder::value).toList();
    }

    public static void registerCatalyst(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(YTechItems.PRIMITIVE_ALLOY_SMELTER.get()), RECIPE_TYPE, RecipeTypes.FUELING);
        registration.addRecipeCatalyst(new ItemStack(YTechItems.BRICK_CHIMNEY.get()), RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(YTechItems.REINFORCED_BRICK_CHIMNEY.get()), RECIPE_TYPE);
    }
}
