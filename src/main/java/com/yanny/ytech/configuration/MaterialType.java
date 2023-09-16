package com.yanny.ytech.configuration;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public enum MaterialType {
    //solid elements
    COPPER("copper", "Copper", hex2rgb("#B87333"), 1085, Tiers.STONE, ToolType.PICKAXE),
    GOLD("gold", "Gold", hex2rgb("#FFDF00"), 1064, Tiers.GOLD, ToolType.PICKAXE),
    IRON("iron", "Iron", hex2rgb("#AAAAAA"), 1538, Tiers.IRON, ToolType.PICKAXE),
    TIN("tin", "Tin", hex2rgb("#808080"), 232, Tiers.STONE, ToolType.PICKAXE),

    //fluid elements
    MERCURY("mercury", "Mercury", hex2rgb("#DBCECA"), Tiers.DIAMOND, ToolType.PICKAXE),

    //alloys
    BRONZE("bronze", "Bronze", hex2rgb("#D89940"), 913, Tiers.IRON, ToolType.PICKAXE),

    //ores
    CASSITERITE("cassiterite", "Cassiterite", hex2rgb("#3D3D3D"), 1127, Tiers.STONE, ToolType.PICKAXE),

    //woods
    ACACIA_WOOD("acacia", "Acacia", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    BIRCH_WOOD("birch", "Birch", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    CHERRY_WOOD("cherry", "Cherry", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    DARK_OAK_WOOD("dark_oak", "Dark Oak", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    JUNGLE_WOOD("jungle", "Jungle", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    MANGROVE_WOOD("mangrove", "Mangrove", -1, "wooden", Tiers.WOOD, ToolType.AXE),
    OAK_WOOD("oak", "Oak", hex2rgb("#C4A570"), "wooden", Tiers.WOOD, ToolType.AXE),
    SPRUCE_WOOD("spruce", "Spruce", -1, "wooden", Tiers.WOOD, ToolType.AXE),

    FLINT("flint", "Flint", hex2rgb("#666666"), Tiers.WOOD, ToolType.PICKAXE),
    STONE("stone", "Stone", hex2rgb("#999999"), Tiers.STONE, ToolType.PICKAXE),
    ;

    public static final EnumSet<MaterialType> ALL_WOODS = EnumSet.of(ACACIA_WOOD, BIRCH_WOOD, CHERRY_WOOD, DARK_OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD, OAK_WOOD, SPRUCE_WOOD);
    public static final EnumSet<MaterialType> ALL_METALS = EnumSet.of(BRONZE, COPPER, GOLD, IRON, TIN);
    public static final EnumSet<MaterialType> ALL_ORES = EnumSet.of(COPPER, GOLD, IRON, CASSITERITE);
    public static final EnumSet<MaterialType> ALL_FLUIDS = EnumSet.noneOf(MaterialType.class);
    public static final EnumSet<MaterialType> TOOL_METALS = EnumSet.of(BRONZE, COPPER, IRON); // strong enough for tool making
    public static final EnumSet<MaterialType> VANILLA_METALS = EnumSet.of(COPPER, GOLD, IRON);

    @NotNull public final String key;
    @NotNull public final String name;
    public final int color;
    public final int meltingTemp;
    @NotNull public final String group;
    @NotNull public final Tier tier;
    @NotNull public final ToolType tool;

    MaterialType(@NotNull String key, @NotNull String name, int color, int meltingTemp, @NotNull Tier tier, @NotNull ToolType tool) {
        this.key = key;
        this.name = name;
        this.color = color;
        this.meltingTemp = meltingTemp;
        this.group = key;
        this.tier = tier;
        this.tool = tool;
    }

    MaterialType(@NotNull String key, @NotNull String name, int color, @NotNull Tier tier, @NotNull ToolType tool) {
        this.key = key;
        this.name = name;
        this.color = color;
        this.meltingTemp = Integer.MAX_VALUE;
        this.group = key;
        this.tier = tier;
        this.tool = tool;
    }

    MaterialType(@NotNull String key, @NotNull String name, int color, @NotNull String group, @NotNull Tier tier, @NotNull ToolType tool) {
        this.key = key;
        this.name = name;
        this.color = color;
        this.meltingTemp = Integer.MAX_VALUE;
        this.group = group;
        this.tier = tier;
        this.tool = tool;
    }

    private static int hex2rgb(@NotNull String colorStr) {
        return Integer.valueOf( colorStr.substring( 1, 3 ), 16 ) << 16 |
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ) << 8 |
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 );
    }
}
