package com.yanny.ytech.configuration;

import net.minecraft.resources.ResourceLocation;

public record TextureHolder(
        int tintIndex,
        int color,
        ResourceLocation texture
) {}
