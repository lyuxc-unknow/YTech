package com.yanny.ytech.registration;

import com.yanny.ytech.configuration.*;

import java.util.HashMap;

public record RegistrationHolder(
        HashMap<MaterialItemType, HashMap<MaterialType, Holder.ItemHolder>> items,
        HashMap<MaterialBlockType, HashMap<MaterialType, Holder.BlockHolder>> blocks,
        HashMap<MaterialFluidType, HashMap<MaterialType, Holder.FluidHolder>> fluids,
        HashMap<SimpleItemType, Holder.SimpleItemHolder> simpleItems,
        HashMap<SimpleBlockType, Holder.SimpleBlockHolder> simpleBlocks
) {
    public RegistrationHolder() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
}
