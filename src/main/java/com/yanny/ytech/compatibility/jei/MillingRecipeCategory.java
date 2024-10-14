package com.yanny.ytech.compatibility.jei;

import com.yanny.ytech.YTechMod;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.recipe.MillingRecipe;
import com.yanny.ytech.registration.YTechItems;
import com.yanny.ytech.registration.YTechRecipeTypes;
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

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MillingRecipeCategory implements IRecipeCategory<MillingRecipe> {
    public static final RecipeType<MillingRecipe> RECIPE_TYPE = RecipeType.create(YTechMod.MOD_ID, "milling", MillingRecipe.class);
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    private final Font font = Minecraft.getInstance().font;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public MillingRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = Utils.modLoc("textures/gui/jei.png");
        background = guiHelper.createDrawable(location, 0, 200, 82, 47);
        icon = guiHelper.createDrawableItemStack(new ItemStack(YTechItems.MILLSTONE.get()));
        localizedName = Component.translatable("gui.ytech.category.milling");
    }

    @NotNull
    @Override
    public RecipeType<MillingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MillingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9).addIngredients(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61,  9).addItemStack(recipe.result());
    }

    @Override
    public void draw(@NotNull MillingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.bonusChance() > 0) {
            guiGraphics.drawString(font, "+1 " + NUMBER_FORMAT.format(recipe.bonusChance() * 100) + "%", 40, 36, 0xFF808080, false);
        }
    }

    public static List<MillingRecipe> getRecipes(@NotNull RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(YTechRecipeTypes.MILLING.get()).stream().map(RecipeHolder::value).toList();
    }

    public static void registerCatalyst(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(YTechItems.MILLSTONE.get()), MillingRecipeCategory.RECIPE_TYPE);
    }
}
