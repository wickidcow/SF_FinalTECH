package io.taraxacum.finaltech.setup;

import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.group.MainItemGroup;
import io.taraxacum.finaltech.core.group.SubFlexItemGroup;
import io.taraxacum.finaltech.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 */
public final class FinalTechMenus {
    // TODO head texture

    /* Slimefun item group */
    public static final NestedItemGroup MAIN_MENU = new NestedItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_CATEGORY_MAIN"), new CustomItemStack(Material.OBSERVER, FinalTechChanged.getLanguageString("categories", "_FINALTECH_ITEM_GROUP", "name"))) {
        @Override
        public boolean isVisible(@Nonnull Player p, @Nonnull PlayerProfile profile, @Nonnull SlimefunGuideMode mode) {
            return false;
        }
    };
    public static final SubItemGroup MENU_ITEMS = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_ITEM"), MAIN_MENU, new CustomItemStack(Material.AMETHYST_SHARD, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_ITEM", "name")));
    public static final SubItemGroup MENU_ELECTRICITY_SYSTEM = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_ELECTRIC_SYSTEM"), MAIN_MENU, new CustomItemStack(Material.BROWN_MUSHROOM_BLOCK, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_ELECTRICITY_SYSTEM", "name")));
    public static final SubItemGroup MENU_CARGO_SYSTEM = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_CARGO_SYSTEM"), MAIN_MENU, new CustomItemStack(Material.COBWEB, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_CARGO_SYSTEM", "name")));
    public static final SubItemGroup MENU_FUNCTIONAL_MACHINE = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_FUNCTIONAL_MACHINE"), MAIN_MENU, new CustomItemStack(Material.LECTERN, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_FUNCTIONAL_MACHINE", "name")));
    public static final SubItemGroup MENU_PRODUCTIVE_MACHINE = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_PRODUCTIVE_MACHINE"), MAIN_MENU, new CustomItemStack(Material.SPAWNER, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_PRODUCTIVE_MACHINE", "name")));
    public static final SubItemGroup MENU_DISC = new SubItemGroup(new NamespacedKey(FinalTechChanged.getInstance(), "_FINALTECH_DISC"), MAIN_MENU, new CustomItemStack(Material.GILDED_BLACKSTONE, FinalTechChanged.getLanguageString("categories", "_FINALTECH_MAIN_MENU_DISC", "name")));

    /* My item group */
    public static final MainItemGroup MAIN_ITEM_GROUP = ConfigUtil.getMainItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_ITEM_GROUP", Material.OBSERVER, "FINAL TECH", "Advanced automation, logistics and end-game technology.");
    // item
    public static final SubFlexItemGroup MAIN_MENU_ITEM = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_ITEM", Material.AMETHYST_SHARD, "Item", "Materials, logic, tools, consumables and weapons.");
    public static final SubFlexItemGroup SUB_MENU_MATERIAL = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_MATERIAL", Material.FLINT, "Material", "Crafting materials and components used by FinalTECH.");
    public static final SubFlexItemGroup SUB_MENU_LOGIC_ITEM = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_LOGIC_ITEM", Material.STICK, "Logic Item", "Logic and digital components for advanced systems.");
    public static final SubFlexItemGroup SUB_MENU_TOOL = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_TOOL", Material.SPYGLASS, "Tool", "Portable tools for configuring and managing FinalTECH.");
    public static final SubFlexItemGroup SUB_MENU_CONSUMABLE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_CONSUMABLE", Material.DRAGON_BREATH, "Consumable", "Single-use cards and utility consumables.");
    public static final SubFlexItemGroup SUB_MENU_WEAPON = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_WEAPON", Material.IRON_PICKAXE, "Weapon", "Advanced FinalTECH tools and combat equipment.");

    // electricity system
    public static final SubFlexItemGroup MAIN_MENU_ELECTRICITY_SYSTEM = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_ELECTRICITY_SYSTEM", Material.BROWN_MUSHROOM_BLOCK, "Electric System", "Power generation, storage, transmission and acceleration.");
    public static final SubFlexItemGroup SUB_MENU_ELECTRIC_GENERATOR = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ELECTRIC_GENERATOR", Material.GLOWSTONE, "Electric Generator", "Machines that generate energy for FinalTECH systems.");
    public static final SubFlexItemGroup SUB_MENU_ELECTRIC_STORAGE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ELECTRIC_STORAGE", Material.YELLOW_STAINED_GLASS, "Electric Storage", "Capacitors and devices for storing large amounts of energy.");
    public static final SubFlexItemGroup SUB_MENU_ELECTRIC_TRANSMISSION = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ELECTRIC_TRANSMISSION", Material.DISPENSER, "Electric Transmission", "Move and distribute energy between machines.");
    public static final SubFlexItemGroup SUB_MENU_ELECTRIC_ACCELERATOR = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ELECTRIC_ACCELERATOR", Material.TARGET, "Electric Accelerator", "Advanced devices for controlling machine operation speed.");

    // cargo system
    public static final SubFlexItemGroup MAIN_MENU_CARGO_SYSTEM = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_CARGO_SYSTEM", Material.COBWEB, "Cargo System", "Storage, routing, filtering and automated item transport.");
    public static final SubFlexItemGroup SUB_MENU_STORAGE_UNIT = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_STORAGE_UNIT", Material.GLASS, "Storage Unit", "Compact storage devices for large quantities of items.");
    public static final SubFlexItemGroup SUB_MENU_ADVANCED_STORAGE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ADVANCED_STORAGE", Material.BOOKSHELF, "Advanced Storage", "Higher-capacity and specialized storage technology.");
    public static final SubFlexItemGroup SUB_MENU_ACCESSOR = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ACCESSOR", Material.REDSTONE_LAMP, "Accessor", "Remote access and interaction tools for machines and storage.");
    public static final SubFlexItemGroup SUB_MENU_LOGIC = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_LOGIC", Material.CHISELED_STONE_BRICKS, "Logic", "Control cargo behavior with conditions and routing logic.");
    public static final SubFlexItemGroup SUB_MENU_CARGO = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_CARGO", Material.END_ROD, "Cargo", "Move, filter and distribute items automatically.");

    // functional machine
    public static final SubFlexItemGroup MAIN_MENU_FUNCTIONAL_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_FUNCTIONAL_MACHINE", Material.LECTERN, "Functional Machine", "Utility machines for specialized automation tasks.");
    public static final SubFlexItemGroup SUB_MENU_CORE_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_CORE_MACHINE", Material.AMETHYST_BLOCK, "Core Machine", "Core devices used by multiple FinalTECH systems.");
    public static final SubFlexItemGroup SUB_MENU_SPECIAL_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_SPECIAL_MACHINE", Material.CHISELED_POLISHED_BLACKSTONE, "Special Machine", "Special-purpose machines with unique functions.");
    public static final SubFlexItemGroup SUB_MENU_TOWER = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_TOWER", Material.RED_GLAZED_TERRACOTTA, "Tower", "Tower-style machines for specialized utility functions.");

    // productive machine
    public static final SubFlexItemGroup MAIN_MENU_PRODUCTIVE_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_PRODUCTIVE_MACHINE", Material.CAULDRON, "Productive Machine", "Machines that process materials and produce resources.");
    public static final SubFlexItemGroup SUB_MENU_MANUAL_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_MANUAL_MACHINE", Material.CRAFTING_TABLE, "Manual Machine", "Player-operated machines for direct processing.");
    public static final SubFlexItemGroup SUB_MENU_BASIC_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_BASIC_MACHINE", Material.OBSIDIAN, "Basic Machine", "Entry-level automated processing machines.");
    public static final SubFlexItemGroup SUB_MENU_ADVANCED_MACHINE = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_ADVANCED_MACHINE", Material.FURNACE, "Advanced Machine", "Higher-tier machines for faster or specialized processing.");
    public static final SubFlexItemGroup SUB_MENU_CONVERSION = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_CONVERSION", Material.BONE_BLOCK, "Conversion", "Convert items, materials or values into other forms.");
    public static final SubFlexItemGroup SUB_MENU_EXTRACTION = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_EXTRACTION", Material.RED_NETHER_BRICKS, "Extraction", "Extract resources and useful products from materials.");
    public static final SubFlexItemGroup SUB_MENU_GENERATOR = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_GENERATOR", Material.BLUE_TERRACOTTA, "Generator", "Generate materials and resources automatically.");

    // final stage item
    public static final SubFlexItemGroup MAIN_MENU_DISC = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_MAIN_MENU_DISC", Material.GILDED_BLACKSTONE, "Disc", "End-game items, trophies and legacy content.");
    public static final SubFlexItemGroup SUB_MENU_FINAL_ITEM = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_FINAL_ITEM", Material.BLACK_CONCRETE, "Final Item", "Highest-tier components and end-game FinalTECH items.");
    public static final SubFlexItemGroup SUB_MENU_TROPHY = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_TROPHY", Material.LARGE_AMETHYST_BUD, "Trophy", "Collectible rewards and acknowledgements.");
    public static final SubFlexItemGroup SUB_MENU_DEPRECATED = ConfigUtil.getSubFlexItemGroup(FinalTechChanged.getLanguageManager(), "_FINALTECH_SUB_MENU_DEPRECATED", Material.CRACKED_STONE_BRICKS, "Deprecated", "Legacy items kept for compatibility; avoid new use.");
}
