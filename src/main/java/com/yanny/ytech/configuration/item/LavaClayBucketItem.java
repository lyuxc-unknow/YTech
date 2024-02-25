package com.yanny.ytech.configuration.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import static com.yanny.ytech.configuration.SimpleItemType.CLAY_BUCKET;
import static com.yanny.ytech.registration.Registration.item;

public class LavaClayBucketItem extends ClayBucketItem {
    public LavaClayBucketItem() {
        super(() -> Fluids.LAVA, new Item.Properties().craftRemainder(item(CLAY_BUCKET)).stacksTo(1));
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 20000;
    }
}
