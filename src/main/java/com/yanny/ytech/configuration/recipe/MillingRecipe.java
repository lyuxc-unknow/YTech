package com.yanny.ytech.configuration.recipe;

import com.google.gson.JsonObject;
import com.yanny.ytech.configuration.Utils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public record MillingRecipe(ResourceLocation id, Ingredient ingredient, int millingTime, ItemStack result) implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<MillingRecipe> RECIPE_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Utils.modLoc("milling").toString();
        }
    };

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @NotNull
    @Override
    public RecipeType<?> getType() {
        return RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<MillingRecipe> {
        @NotNull
        @Override
        public MillingRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
            Ingredient ingredient = Ingredient.fromJson(serializedRecipe.get("ingredient"), false);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));
            int millingTime = GsonHelper.getAsInt(serializedRecipe, "millingTime");
            return new MillingRecipe(recipeId, ingredient, millingTime, result);
        }

        @Override
        public @Nullable MillingRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int millingTime = buffer.readInt();
            return new MillingRecipe(recipeId, ingredient, millingTime, result);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull MillingRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeInt(recipe.millingTime);
        }
    }

    public record Result(@NotNull ResourceLocation id, @NotNull Ingredient ingredient, int millingTime, @NotNull Item result,
                         @NotNull Advancement.Builder advancement, @NotNull ResourceLocation advancementId) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add("ingredient", ingredient.toJson());

            JsonObject resultItemStack = new JsonObject();
            resultItemStack.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(result)).toString());
            json.add("result", resultItemStack);

            json.addProperty("millingTime", millingTime);
        }

        @NotNull
        @Override
        public ResourceLocation getId() {
            return id;
        }

        @NotNull
        @Override
        public RecipeSerializer<?> getType() {
            return SERIALIZER;
        }

        @NotNull
        @Override
        public JsonObject serializeAdvancement() {
            return advancement.serializeToJson();
        }

        @NotNull
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }

    public static class Builder implements RecipeBuilder {
        private final Ingredient ingredient;
        private final int millingTime;
        private final Item result;
        private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

        Builder(@NotNull Ingredient ingredient, int millingTime, @NotNull Item result) {
            this.ingredient = ingredient;
            this.millingTime = millingTime;
            this.result = result;
        }

        public static Builder milling(@NotNull TagKey<Item> input, int millingTime, @NotNull Item result) {
            return new Builder(Ingredient.of(input), millingTime, result);
        }

        public static Builder milling(@NotNull ItemLike input, int millingTime, @NotNull Item result) {
            return new Builder(Ingredient.of(input), millingTime, result);
        }

        @NotNull
        @Override
        public RecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull CriterionTriggerInstance criterionTrigger) {
            this.advancement.addCriterion(criterionName, criterionTrigger);
            return this;
        }

        @NotNull
        @Override
        public RecipeBuilder group(@Nullable String groupName) {
            return this;
        }

        @NotNull
        @Override
        public Item getResult() {
            return result;
        }

        @Override
        public void save(@NotNull Consumer<FinishedRecipe> finishedRecipeConsumer, @NotNull ResourceLocation recipeId) {
            ensureValid(recipeId);
            advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe",
                    RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
            finishedRecipeConsumer.accept(new MillingRecipe.Result(recipeId, ingredient, millingTime, result, advancement, recipeId.withPrefix("recipes/milling/")));
        }

        //Makes sure that this recipe is valid and obtainable.
        private void ensureValid(@NotNull ResourceLocation id) {
            if (this.advancement.getCriteria().isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }
    }
}
