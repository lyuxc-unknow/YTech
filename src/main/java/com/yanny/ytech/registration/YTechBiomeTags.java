package com.yanny.ytech.registration;

import com.yanny.ytech.configuration.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class YTechBiomeTags {
    public static final TagKey<Biome> AUROCHS_BIOMES = create("aurochs_native_biome");
    public static final TagKey<Biome> DEER_BIOMES = create("deer_native_biome");
    public static final TagKey<Biome> FOWL_BIOMES = create("fowl_native_biome");
    public static final TagKey<Biome> MOUFLON_BIOMES = create("mouflon_native_biome");
    public static final TagKey<Biome> SABER_TOOTH_TIGER_BIOMES = create("saber_tooth_tiger_native_biome");
    public static final TagKey<Biome> WILD_BOAR_BIOMES = create("wild_boar_native_biome");
    public static final TagKey<Biome> WOOLLY_MAMMOTH_BIOMES = create("woolly_mammoth_native_biome");
    public static final TagKey<Biome> WOOLLY_RHINO_BIOMES = create("woolly_rhino_native_biome");

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, Utils.modLoc(name));
    }
}
