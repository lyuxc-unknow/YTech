package com.yanny.ytech.configuration.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ytech.registration.YTechRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RemainingPartShapelessRecipe extends ShapelessRecipe {
    private static final RandomSource RANDOM = RandomSource.create();

    public RemainingPartShapelessRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, p_250689_);
    }

    public RemainingPartShapelessRecipe(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(null), recipe.getIngredients());
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container) {
        NonNullList<ItemStack> list = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < list.size(); ++i) {
            ItemStack item = container.getItem(i);

            if (item.hasCraftingRemainingItem()) {
                list.set(i, item.getCraftingRemainingItem());
            } else if (item.isDamageableItem()) {
                ItemStack result = item.copy();
                list.set(i, result);

                result.hurtAndBreak(1, RANDOM, null, () -> {
                    result.shrink(1);
                    result.setDamageValue(0);
                });
            }
        }

        return list;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return YTechRecipeSerializers.REMAINING_PART_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<RemainingPartShapelessRecipe> {
        private static final MapCodec<RemainingPartShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
                (instance) -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((recipe) -> recipe.category),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result),
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                                (ingredients) -> {
                                    Ingredient[] aingredient = ingredients.toArray(Ingredient[]::new);

                                    if (aingredient.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else {
                                        return aingredient.length > 3*3 ? DataResult.error(() -> {
                                            return "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(3*3);
                                        }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                    }
                                },
                                DataResult::success).forGetter(ShapelessRecipe::getIngredients)
                ).apply(instance, RemainingPartShapelessRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, RemainingPartShapelessRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @NotNull
        @Override
        public MapCodec<RemainingPartShapelessRecipe> codec() {
            return CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RemainingPartShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        @NotNull
        private static RemainingPartShapelessRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf friendlyByteBuf) {
            return new RemainingPartShapelessRecipe(ShapelessRecipe.Serializer.fromNetwork(friendlyByteBuf));
        }

        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull RemainingPartShapelessRecipe recipe) {
            ShapelessRecipe.Serializer.toNetwork(buf, recipe);
        }
    }

    public static class Builder extends ShapelessRecipeBuilder {
        public Builder(RecipeCategory pCategory, ItemLike pResult, int pCount) {
            super(pCategory, pResult, pCount);
        }

        public static Builder shapeless(@NotNull RecipeCategory pCategory, ItemLike pResult) {
            return new Builder(pCategory, pResult, 1);
        }

        public static Builder shapeless(@NotNull RecipeCategory pCategory, ItemLike pResult, int pCount) {
            return new Builder(pCategory, pResult, pCount);
        }

        @Override
        public void save(@NotNull RecipeOutput consumer, @NotNull ResourceLocation id) {
            super.save(new RecipeOutput() {
                @NotNull
                @Override
                public Advancement.Builder advancement() {
                    return consumer.advancement();
                }

                @Override
                public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, ICondition @NotNull ... iConditions) {
                    consumer.accept(id, new RemainingPartShapelessRecipe((ShapelessRecipe) recipe), advancementHolder);
                }
            }, id);
        }
    }
}
