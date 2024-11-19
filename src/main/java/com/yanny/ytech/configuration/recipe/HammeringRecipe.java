package com.yanny.ytech.configuration.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ytech.registration.YTechRecipeSerializers;
import com.yanny.ytech.registration.YTechRecipeTypes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public record HammeringRecipe(Ingredient ingredient, Ingredient tool, int hitCount, ItemStack result) implements Recipe<SingleRecipeInput> {
    @Override
    public boolean matches(@NotNull SingleRecipeInput recipeInput, @NotNull Level level) {
        return ingredient.test(recipeInput.getItem(0));
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull SingleRecipeInput recipeInput, @NotNull HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return result;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return YTechRecipeSerializers.HAMMERING.get();
    }

    @NotNull
    @Override
    public RecipeType<?> getType() {
        return YTechRecipeTypes.HAMMERING.get();
    }

    public static class Serializer implements RecipeSerializer<HammeringRecipe> {
        private static final MapCodec<HammeringRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((hammeringRecipe) -> hammeringRecipe.ingredient),
                        Ingredient.CODEC.fieldOf("tool").forGetter((hammeringRecipe) -> hammeringRecipe.tool),
                        Codec.INT.fieldOf("hitCount").forGetter((tanningRecipe) -> tanningRecipe.hitCount),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((hammeringRecipe) -> hammeringRecipe.result)
                ).apply(instance, HammeringRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, HammeringRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        @NotNull
        public MapCodec<HammeringRecipe> codec() {
            return CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HammeringRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        @NotNull
        private static HammeringRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            int hitCount = buffer.readInt();
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new HammeringRecipe(ingredient, tool, hitCount, result);
        }

        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buffer, @NotNull HammeringRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tool);
            buffer.writeInt(recipe.hitCount);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }

    public static class Builder implements RecipeBuilder {
        private final Ingredient ingredient;
        private Ingredient tool = Ingredient.EMPTY;
        int hitCount;
        private final Item result;
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

        Builder(@NotNull Ingredient ingredient, int hitCount, @NotNull Item result) {
            this.ingredient = ingredient;
            this.hitCount = hitCount;
            this.result = result;
        }

        public static Builder hammering(@NotNull TagKey<Item> input, int hitCount, @NotNull Item result) {
            return new Builder(Ingredient.of(input), hitCount, result);
        }

        public static Builder hammering(@NotNull ItemLike input, int hitCount, @NotNull Item result) {
            return new Builder(Ingredient.of(input), hitCount, result);
        }

        public Builder tool(@NotNull Ingredient tool) {
            this.tool = tool;
            return this;
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
            finishedRecipeConsumer.accept(
                    recipeId,
                    new HammeringRecipe(ingredient, tool, hitCount, new ItemStack(result)),
                    builder.build(recipeId.withPrefix("recipes/hammering/"))
            );
        }

        //Makes sure that this recipe is valid and obtainable.
        private void ensureValid(@NotNull ResourceLocation id) {
            if (this.criteria.isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }
    }
}
