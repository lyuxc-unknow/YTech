package com.yanny.ytech.configuration.recipe;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ytech.configuration.Utils;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public record BlockHitRecipe(Ingredient ingredient, Ingredient block, ItemStack result) implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<BlockHitRecipe> RECIPE_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Utils.modLoc("block_hit").toString();
        }
    };

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0)) && block.test(container.getItem(1));
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
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @NotNull
    @Override
    public RecipeType<?> getType() {
        return RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<BlockHitRecipe> {
        private static final Codec<BlockHitRecipe> CODEC = RecordCodecBuilder.create((recipe) -> recipe.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((blockHitRecipe) -> blockHitRecipe.ingredient),
                Ingredient.CODEC_NONEMPTY.fieldOf("block").forGetter((blockHitRecipe) -> blockHitRecipe.block),
                ExtraCodecs.either(BuiltInRegistries.ITEM.byNameCodec(), CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC)
                        .xmap((either) -> either.map(ItemStack::new, Function.identity()), (stack) -> {
                            if (stack.getCount() != 1) {
                                return Either.right(stack);
                            } else {
                                return ItemStack.matches(stack, new ItemStack(stack.getItem())) ? Either.left(stack.getItem()) : Either.right(stack);
                            }
                        }).fieldOf("result").forGetter((blockHitRecipe) -> blockHitRecipe.result)
        ).apply(recipe, BlockHitRecipe::new));

        @Override
        @NotNull
        public Codec<BlockHitRecipe> codec() {
            return CODEC;
        }

        @Override
        @NotNull
        public BlockHitRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient block = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            return new BlockHitRecipe(ingredient, block, result);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BlockHitRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            recipe.block.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }

    public record Result(@NotNull ResourceLocation id, @NotNull Ingredient ingredient, @NotNull Ingredient block, @NotNull Item result,
                         @NotNull AdvancementHolder advancementHolder) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add("ingredient", ingredient.toJson(false));
            json.add("block", block.toJson(false));

            JsonObject resultItemStack = new JsonObject();
            resultItemStack.addProperty("item", Utils.loc(result).toString());
            json.add("result", resultItemStack);
        }

        @NotNull
        @Override
        public AdvancementHolder advancement() {
            return advancementHolder;
        }

        @NotNull
        @Override
        public RecipeSerializer<?> type() {
            return SERIALIZER;
        }
    }

    public static class Builder implements RecipeBuilder {
        private final Ingredient ingredient;
        private final Ingredient block;
        private final Item result;
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

        Builder(@NotNull Ingredient ingredient, @NotNull Ingredient block, @NotNull Item result) {
            this.ingredient = ingredient;
            this.block = block;
            this.result = result;
        }

        public static Builder blockUse(@NotNull TagKey<Item> input, TagKey<Item> block, @NotNull Item result) {
            return new Builder(Ingredient.of(input), Ingredient.of(block), result);
        }

        public static Builder blockUse(@NotNull ItemLike input, TagKey<Item> block, @NotNull Item result) {
            return new Builder(Ingredient.of(input), Ingredient.of(block), result);
        }

        public static Builder blockUse(@NotNull ItemLike input, ItemLike block, @NotNull Item result) {
            return new Builder(Ingredient.of(input), Ingredient.of(block), result);
        }

        @NotNull
        @Override
        public Builder unlockedBy(@NotNull String criterionName, @NotNull Criterion criterionTrigger) {
            this.criteria.put(criterionName, criterionTrigger);
            return this;
        }

        @NotNull
        @Override
        public Builder group(@Nullable String groupName) {
            return this;
        }

        @NotNull
        @Override
        public Item getResult() {
            return result;
        }

        @Override
        public void save(@NotNull RecipeOutput finishedRecipeConsumer, @NotNull ResourceLocation recipeId) {
            ensureValid(recipeId);
            Advancement.Builder builder = finishedRecipeConsumer.advancement().addCriterion("has_the_recipe",
                    RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(builder::addCriterion);
            finishedRecipeConsumer.accept(new BlockHitRecipe.Result(recipeId, ingredient, block, result, builder.build(recipeId.withPrefix("recipes/block_hit/"))));
        }

        //Makes sure that this recipe is valid and obtainable.
        private void ensureValid(@NotNull ResourceLocation id) {
            if (this.criteria.isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }
    }
}
