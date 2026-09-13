package io.taraxacum.finaltech.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.libs.plugin.dto.LanguageManager;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builds short, player-facing purpose text for FinalTECH items in the Guide.
 *
 * <p>The original addon keeps most explanations under items.&lt;id&gt;.info, so the
 * category browser historically showed a name and internal id with no clue what
 * an item actually does. This helper promotes stable Usage/Mechanism text into
 * the browser and supplies conservative English fallbacks for items which only
 * document their recipe or production method.</p>
 */
public final class GuideItemLoreUtil {
    private static final int MAX_PURPOSE_LINES = 2;
    private static final Pattern DYNAMIC_PLACEHOLDER = Pattern.compile("\\{\\d+}");

    private static final Map<String, String> EXACT_FALLBACKS = Map.ofEntries(
            Map.entry("_FINALTECH_GEARWHEEL", "Crafting component used throughout FinalTECH machines and technology."),
            Map.entry("_FINALTECH_UNORDERED_DUST", "Intermediate material refined into Ordered Dust for FinalTECH crafting."),
            Map.entry("_FINALTECH_ORDERED_DUST", "Refined crafting material used in higher-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_BUG", "Special progression material produced through Equivalent Exchange."),
            Map.entry("_FINALTECH_ENTROPY", "Core resource used by FinalTECH logic and advanced crafting systems."),
            Map.entry("_FINALTECH_ETHER", "GEO resource mined by the Ether Miner for advanced FinalTECH recipes."),
            Map.entry("_FINALTECH_BOX", "Rare progression item obtained through FinalTECH's high-altitude death mechanic."),
            Map.entry("_FINALTECH_SHINE", "Rare void-progression item obtained while carrying a Box below the world."),
            Map.entry("_FINALTECH_ANNULAR", "Advanced crafting component created with the Card Operation Table."),
            Map.entry("_FINALTECH_SINGULARITY", "High-tier component used by FinalTECH serialization and end-game recipes."),
            Map.entry("_FINALTECH_SPIROCHETE", "High-tier component used by FinalTECH serialization and end-game recipes."),
            Map.entry("_FINALTECH_PHONY", "High-tier component used by serialization and Card Operation recipes."),
            Map.entry("_FINALTECH_JUSTIFIABILITY", "End-game crafting component obtained through the Entropy Seed."),
            Map.entry("_FINALTECH_EQUIVALENT_CONCEPT", "End-game crafting component obtained through the Entropy Seed."),
            Map.entry("_FINALTECH_BEDROCK_CRAFT_TABLE", "Crafting station for Bedrock Craft Table recipes used by many FinalTECH machines."),
            Map.entry("_FINALTECH_MATRIX_CRAFTING_TABLE", "End-game crafting station for Matrix-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_ITEM_DISMANTLE_TABLE", "Breaks supported items back down into component materials."),
            Map.entry("_FINALTECH_AUTO_ITEM_DISMANTLE_TABLE", "Automates dismantling supported items back into components."),
            Map.entry("_FINALTECH_CARD_OPERATION_TABLE", "Creates, copies and modifies FinalTECH cards and card components."),
            Map.entry("_FINALTECH_COBBLESTONE_FACTORY", "Produces cobblestone automatically for resource-processing setups."),
            Map.entry("_FINALTECH_CRUCIBLE", "Manual processing station for Crucible recipes."),
            Map.entry("_FINALTECH_BASIC_LOGIC_FACTORY", "Produces and processes basic logic resources used by FinalTECH."),
            Map.entry("_FINALTECH_GRAVEL_CONVERSION", "Automates Gold Pan-style processing for gravel resources."),
            Map.entry("_FINALTECH_SOUL_SAND_CONVERSION", "Automates Nether Gold Pan-style processing for soul sand resources."),
            Map.entry("_FINALTECH_LOGIC_TO_DIGITAL_CONVERSION", "Converts FALSE/TRUE logic into digital values 0/1."),
            Map.entry("_FINALTECH_DIGITAL_EXTRACTION", "Extracts digital number tokens from FinalTECH logic values."),
            Map.entry("_FINALTECH_LIQUID_CARD_GENERATOR", "Generates reusable liquid cards for supported FinalTECH machines."),
            Map.entry("_FINALTECH_LOGIC_GENERATOR", "Generates TRUE/FALSE logic tokens for FinalTECH logic systems."),
            Map.entry("_FINALTECH_DIGITAL_GENERATOR", "Generates digital number tokens for FinalTECH logic systems."),
            Map.entry("_FINALTECH_MATRIX_MACHINE_ACCELERATE_CARD", "End-game card that immediately advances supported machine operation."),
            Map.entry("_FINALTECH_MATRIX_ITEM_DISMANTLE_TABLE", "Matrix-tier machine for high-end item dismantling."),
            Map.entry("_FINALTECH_MATRIX_EXPANDED_CAPACITOR", "Matrix-tier capacitor for extremely large energy storage."));

    private GuideItemLoreUtil() {
    }

    /**
     * Creates the icon used while browsing FinalTECH categories.
     */
    @Nonnull
    public static ItemStack createGuideIcon(@Nonnull SlimefunItem slimefunItem, boolean cheatMode) {
        ItemStack icon = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.DARK_GRAY + "Purpose");
        lore.addAll(getPurposeLore(slimefunItem));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + slimefunItem.getId());

        if (cheatMode) {
            lore.add("");
            lore.add(ChatColor.GREEN + "Click: Give 1");
            lore.add(ChatColor.GREEN + "Shift-click: Give a full stack");
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    /**
     * Returns one or two concise lines explaining what an item is for.
     */
    @Nonnull
    public static List<String> getPurposeLore(@Nonnull SlimefunItem slimefunItem) {
        LanguageManager languageManager = FinalTechChanged.getLanguageManager();
        String id = slimefunItem.getId();

        List<String> explicit = getStableLines(languageManager, "items", id, "guide-lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        // Preserve any normal item lore authors have explicitly supplied.
        explicit = getStableLines(languageManager, "items", id, "lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        List<String> derived = deriveFromInfo(languageManager, id);
        if (!derived.isEmpty()) {
            return derived;
        }

        return List.of(ChatColor.GRAY + fallbackPurpose(id, slimefunItem.getItemName()));
    }

    @Nonnull
    private static List<String> deriveFromInfo(@Nonnull LanguageManager languageManager, @Nonnull String id) {
        for (int i = 1; i <= 12; i++) {
            String section = String.valueOf(i);
            if (!languageManager.containPath("items", id, "info", section, "name")
                    || !languageManager.containPath("items", id, "info", section, "lore")) {
                continue;
            }

            String heading = ChatColor.stripColor(languageManager.getString("items", id, "info", section, "name"));
            String normalizedHeading = heading == null ? "" : heading.toLowerCase(Locale.ROOT);
            if (!normalizedHeading.contains("usage")
                    && !normalizedHeading.contains("mechanism")
                    && !normalizedHeading.contains("function")) {
                continue;
            }

            List<String> lines = getStableLines(languageManager, "items", id, "info", section, "lore");
            if (!lines.isEmpty()) {
                return lines;
            }
        }
        return List.of();
    }

    @Nonnull
    private static List<String> getStableLines(@Nonnull LanguageManager languageManager, @Nonnull String... path) {
        if (!languageManager.containPath(path)) {
            return List.of();
        }

        List<String> result = new ArrayList<>(MAX_PURPOSE_LINES);
        for (String line : languageManager.getStringList(path)) {
            if (line == null || line.isBlank() || DYNAMIC_PLACEHOLDER.matcher(line).find()) {
                continue;
            }
            result.add(line);
            if (result.size() >= MAX_PURPOSE_LINES) {
                break;
            }
        }
        return result;
    }

    @Nonnull
    private static String fallbackPurpose(@Nonnull String id, @Nonnull String itemName) {
        String exact = EXACT_FALLBACKS.get(id);
        if (exact != null) {
            return exact;
        }

        if (id.equals("_FINALTECH_LOGIC_FALSE") || id.equals("_FINALTECH_LOGIC_TRUE")) {
            return "Boolean logic token used by comparators, logic machines and conversions.";
        }
        if (id.startsWith("_FINALTECH_DIGITAL_")) {
            return "Numeric token used by FinalTECH digital logic, arithmetic and conversion machines.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACCELERATE_CARD_")) {
            return "Immediately advances supported machines to speed up their operation.";
        }
        if (id.contains("STORAGE_UNIT")) {
            return "Cargo-compatible item storage; this variant changes how stored slots are organized or accessed.";
        }
        if (id.startsWith("_FINALTECH_MANUAL_")) {
            return "Manual processing station for " + stripPrefix(itemName, "Manual ") + " recipes.";
        }
        if (id.startsWith("_FINALTECH_ADVANCED_")) {
            return "Automated high-tier processor for " + stripPrefix(itemName, "Advanced ") + " recipes.";
        }
        if (id.startsWith("_FINALTECH_MATRIX_")) {
            return "End-game Matrix-tier " + stripPrefix(itemName, "Matrix ") + " for advanced FinalTECH automation.";
        }
        if (id.endsWith("_CONVERSION")) {
            return "Converts supported FinalTECH resources into another usable form.";
        }
        if (id.endsWith("_EXTRACTION")) {
            return "Extracts useful FinalTECH resources from supported inputs.";
        }
        if (id.endsWith("_GENERATOR")) {
            return "Automatically generates resources used by FinalTECH systems.";
        }

        return "Used by FinalTECH crafting, progression or automation; open the item for recipe details.";
    }

    @Nonnull
    private static String stripPrefix(@Nonnull String itemName, @Nonnull String prefix) {
        String plain = ChatColor.stripColor(itemName);
        if (plain == null || plain.isBlank()) {
            return "this machine";
        }
        if (plain.regionMatches(true, 0, prefix, 0, prefix.length())) {
            plain = plain.substring(prefix.length());
        }
        return plain;
    }
}
