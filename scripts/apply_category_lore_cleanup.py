from pathlib import Path
import re

CONFIG_PATH = Path("src/main/java/io/taraxacum/finaltech/util/ConfigUtil.java")
MENUS_PATH = Path("src/main/java/io/taraxacum/finaltech/setup/FinalTechMenus.java")
LANG_PATH = Path("src/main/resources/language/en-US.yml")

CATEGORY_LORE = {
    "_FINALTECH_ITEM_GROUP": ["Advanced automation, logistics and end-game technology."],
    "_FINALTECH_MAIN_MENU_ITEM": ["Materials, logic, tools, consumables and weapons."],
    "_FINALTECH_SUB_MENU_MATERIAL": ["Crafting materials and components used by FinalTECH."],
    "_FINALTECH_SUB_MENU_LOGIC_ITEM": ["Logic and digital components for advanced systems."],
    "_FINALTECH_SUB_MENU_CONSUMABLE": ["Single-use cards and utility consumables."],
    "_FINALTECH_SUB_MENU_TOOL": ["Portable tools for configuring and managing FinalTECH."],
    "_FINALTECH_SUB_MENU_WEAPON": ["Advanced FinalTECH tools and combat equipment."],
    "_FINALTECH_MAIN_MENU_ELECTRICITY_SYSTEM": ["Power generation, storage, transmission and acceleration."],
    "_FINALTECH_SUB_MENU_ELECTRIC_GENERATOR": ["Machines that generate energy for FinalTECH systems."],
    "_FINALTECH_SUB_MENU_ELECTRIC_STORAGE": ["Capacitors and devices for storing large amounts of energy."],
    "_FINALTECH_SUB_MENU_ELECTRIC_TRANSMISSION": ["Move and distribute energy between machines."],
    "_FINALTECH_SUB_MENU_ELECTRIC_ACCELERATOR": ["Advanced devices for controlling machine operation speed."],
    "_FINALTECH_MAIN_MENU_CARGO_SYSTEM": ["Storage, routing, filtering and automated item transport."],
    "_FINALTECH_SUB_MENU_STORAGE_UNIT": ["Compact storage devices for large quantities of items."],
    "_FINALTECH_SUB_MENU_ADVANCED_STORAGE": ["Higher-capacity and specialized storage technology."],
    "_FINALTECH_SUB_MENU_ACCESSOR": ["Remote access and interaction tools for machines and storage."],
    "_FINALTECH_SUB_MENU_LOGIC": ["Control cargo behavior with conditions and routing logic."],
    "_FINALTECH_SUB_MENU_CARGO": ["Move, filter and distribute items automatically."],
    "_FINALTECH_MAIN_MENU_FUNCTIONAL_MACHINE": ["Utility machines for specialized automation tasks."],
    "_FINALTECH_SUB_MENU_CORE_MACHINE": ["Core devices used by multiple FinalTECH systems."],
    "_FINALTECH_SUB_MENU_SPECIAL_MACHINE": ["Special-purpose machines with unique functions."],
    "_FINALTECH_SUB_MENU_TOWER": ["Tower-style machines for specialized utility functions."],
    "_FINALTECH_MAIN_MENU_PRODUCTIVE_MACHINE": ["Machines that process materials and produce resources."],
    "_FINALTECH_SUB_MENU_MANUAL_MACHINE": ["Player-operated machines for direct processing."],
    "_FINALTECH_SUB_MENU_BASIC_MACHINE": ["Entry-level automated processing machines."],
    "_FINALTECH_SUB_MENU_ADVANCED_MACHINE": ["Higher-tier machines for faster or specialized processing."],
    "_FINALTECH_SUB_MENU_CONVERSION": ["Convert items, materials or values into other forms."],
    "_FINALTECH_SUB_MENU_EXTRACTION": ["Extract resources and useful products from materials."],
    "_FINALTECH_SUB_MENU_GENERATOR": ["Generate materials and resources automatically."],
    "_FINALTECH_MAIN_MENU_DISC": ["End-game items, trophies and legacy content."],
    "_FINALTECH_SUB_MENU_FINAL_ITEM": ["Highest-tier components and end-game FinalTECH items."],
    "_FINALTECH_SUB_MENU_TROPHY": ["Collectible rewards and acknowledgements."],
    "_FINALTECH_SUB_MENU_DEPRECATED": ["Legacy items kept for compatibility; avoid new use."],
}


def patch_config_util():
    text = CONFIG_PATH.read_text(encoding="utf-8")
    old = '''    public static MainItemGroup getMainItemGroup(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull Material defaultMaterial, @Nonnull String defaultName) {
        Material material = defaultMaterial;
        if (languageManager.containPath("categories", key, "material")) {
            material = Material.getMaterial(languageManager.getString("categories", key, "material"));
            material = material == null ? defaultMaterial : material;
        }
        String name = languageManager.containPath("categories", key, "name") ? languageManager.getString("categories", key, "name") : defaultName;
        return new MainItemGroup(new NamespacedKey(languageManager.getPlugin(), key), new CustomItemStack(material, name), 0);
    }

    public static SubFlexItemGroup getSubFlexItemGroup(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull Material defaultMaterial, @Nonnull String defaultName) {
        Material material = defaultMaterial;
        if (languageManager.containPath("categories", key, "material")) {
            material = Material.getMaterial(languageManager.getString("categories", key, "material"));
            material = material == null ? defaultMaterial : material;
        }
        String name = languageManager.containPath("categories", key, "name") ? languageManager.getString("categories", key, "name") : defaultName;
        return new SubFlexItemGroup(new NamespacedKey(languageManager.getPlugin(), key), new CustomItemStack(material, name), 0);
    }
'''
    new = '''    @Nonnull
    private static String getCategoryName(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull String defaultName) {
        String path = "categories." + key + ".name";
        String fallbackName = "{color:random}" + defaultName;

        if (!languageManager.containPath("categories", key, "name")) {
            languageManager.setValue(fallbackName, "categories", key, "name");
        }

        String name = languageManager.getString("categories", key, "name");
        if (name.isBlank() || name.equals(path)) {
            languageManager.setValue(fallbackName, "categories", key, "name");
            name = languageManager.getString("categories", key, "name");
        }
        return name;
    }

    @Nonnull
    private static String[] getCategoryLore(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull String... defaultLore) {
        String path = "categories." + key + ".lore";
        List<String> lore = languageManager.containPath("categories", key, "lore")
                ? languageManager.getStringList("categories", key, "lore")
                : List.of();

        if (lore.isEmpty() || (lore.size() == 1 && lore.get(0).equals(path))) {
            List<String> fallbackLore = java.util.Arrays.stream(defaultLore)
                    .map(line -> "{color:normal}" + line)
                    .toList();
            languageManager.setValue(fallbackLore, "categories", key, "lore");
            lore = languageManager.getStringList("categories", key, "lore");
        }

        return lore.toArray(new String[0]);
    }

    public static MainItemGroup getMainItemGroup(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull Material defaultMaterial, @Nonnull String defaultName, @Nonnull String... defaultLore) {
        Material material = defaultMaterial;
        if (languageManager.containPath("categories", key, "material")) {
            material = Material.getMaterial(languageManager.getString("categories", key, "material"));
            material = material == null ? defaultMaterial : material;
        }
        String name = getCategoryName(languageManager, key, defaultName);
        return new MainItemGroup(new NamespacedKey(languageManager.getPlugin(), key), new CustomItemStack(material, name, getCategoryLore(languageManager, key, defaultLore)), 0);
    }

    public static SubFlexItemGroup getSubFlexItemGroup(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nonnull Material defaultMaterial, @Nonnull String defaultName, @Nonnull String... defaultLore) {
        Material material = defaultMaterial;
        if (languageManager.containPath("categories", key, "material")) {
            material = Material.getMaterial(languageManager.getString("categories", key, "material"));
            material = material == null ? defaultMaterial : material;
        }
        String name = getCategoryName(languageManager, key, defaultName);
        return new SubFlexItemGroup(new NamespacedKey(languageManager.getPlugin(), key), new CustomItemStack(material, name, getCategoryLore(languageManager, key, defaultLore)), 0);
    }
'''
    if old not in text:
        raise SystemExit("Expected ConfigUtil category factory block was not found")
    CONFIG_PATH.write_text(text.replace(old, new, 1), encoding="utf-8")


def patch_menu_defaults():
    lines = MENUS_PATH.read_text(encoding="utf-8").splitlines()
    output = []
    patched = set()
    for line in lines:
        if "ConfigUtil.getMainItemGroup" in line or "ConfigUtil.getSubFlexItemGroup" in line:
            match = re.search(r'"(_FINALTECH_[A-Z0-9_]+)"', line)
            if match and match.group(1) in CATEGORY_LORE:
                key = match.group(1)
                if not line.rstrip().endswith(");"):
                    raise SystemExit(f"Unexpected category factory formatting for {key}")
                lore_args = "".join(f', "{entry}"' for entry in CATEGORY_LORE[key])
                line = line.rstrip()[:-2] + lore_args + ");"
                patched.add(key)
        output.append(line)

    missing = set(CATEGORY_LORE) - patched
    if missing:
        raise SystemExit("Menu category calls not found: " + ", ".join(sorted(missing)))
    MENUS_PATH.write_text("\n".join(output) + "\n", encoding="utf-8")


def patch_english_locale():
    lines = LANG_PATH.read_text(encoding="utf-8").splitlines()
    output = []
    in_categories = False
    current_key = None
    patched = set()

    for line in lines:
        if line == "categories:":
            in_categories = True
            current_key = None
            output.append(line)
            continue

        if in_categories:
            key_match = re.match(r"^  (_FINALTECH_[A-Z0-9_]+):$", line)
            if key_match:
                current_key = key_match.group(1)
            elif line and not line.startswith(" "):
                in_categories = False
                current_key = None

        output.append(line)

        if in_categories and current_key in CATEGORY_LORE and line.startswith("    name:"):
            output.append("    lore:")
            for entry in CATEGORY_LORE[current_key]:
                output.append(f"      - '{{color:normal}}{entry}'")
            patched.add(current_key)

    missing = set(CATEGORY_LORE) - patched
    if missing:
        raise SystemExit("English category definitions not found: " + ", ".join(sorted(missing)))

    LANG_PATH.write_text("\n".join(output) + "\n", encoding="utf-8")


patch_config_util()
patch_menu_defaults()
patch_english_locale()
print(f"Added explanatory lore and category-name self-healing for {len(CATEGORY_LORE)} FinalTECH Guide categories.")
