package io.taraxacum.finaltech.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.libs.plugin.dto.LanguageManager;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.interfaces.ShowInfoItem;
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
 * Builds player-facing help text for FinalTECH items in the Slimefun Guide.
 *
 * <p>The original FinalTECH language files store most real documentation in
 * {@code items.<id>.info.<n>.lore}. English translations preserve the majority
 * of those sections, while a smaller set of items only had flavor text such as
 * "Good Things!" or "Fast Things!". This helper restores the useful original
 * Usage/Introduction/Mechanism text to the browse view and supplies verified
 * mechanics-based descriptions only where the original English help is absent.</p>
 */
public final class GuideItemLoreUtil {
    private static final int MAX_PURPOSE_LINES = 4;
    private static final int MAX_LINE_LENGTH = 44;
    private static final Pattern DYNAMIC_PLACEHOLDER = Pattern.compile("\\{\\d+}");
    private static final Pattern STRUCTURAL_PLACEHOLDER = Pattern.compile("^(?:\\[\\s*]|\\{\\s*}|null|~)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FLAVOR_ONLY = Pattern.compile("^(?:good|bad|fast) things!?$", Pattern.CASE_INSENSITIVE);

    /**
     * Source-backed help for items whose original English locale is missing,
     * incomplete, or only contains flavor text. These descriptions were checked
     * against the original zh-CN locale and/or the current item implementation.
     */
    private static final Map<String, String> EXACT_FALLBACKS = Map.ofEntries(
            Map.entry("_FINALTECH_GEARWHEEL", "Crafting component used throughout FinalTECH machines and technology."),
            Map.entry("_FINALTECH_UNORDERED_DUST", "Unstable crafting material paired with Ordered Dust in advanced FinalTECH recipes and Matrix reactions."),
            Map.entry("_FINALTECH_ORDERED_DUST", "Stable crafting material paired with Unordered Dust in advanced FinalTECH recipes and Matrix reactions."),
            Map.entry("_FINALTECH_BUG", "Progression material used by FinalTECH logic, exchange and reactor systems."),
            Map.entry("_FINALTECH_ENTROPY", "Core FinalTECH resource used as a universal material or catalyst in several advanced systems."),
            Map.entry("_FINALTECH_ETHER", "Resource mined by the Ether Miner and used in advanced FinalTECH recipes."),
            Map.entry("_FINALTECH_ANNULAR", "Advanced component produced by the Card Operation Table and used in end-game progression."),
            Map.entry("_FINALTECH_SINGULARITY", "High-tier component produced by item serialization and used by advanced card/reactor recipes."),
            Map.entry("_FINALTECH_SPIROCHETE", "High-tier component produced by item serialization and used by advanced card/reactor recipes."),
            Map.entry("_FINALTECH_PHONY", "Universal high-tier component used by card operations, serialization and Matrix machines."),
            Map.entry("_FINALTECH_JUSTIFIABILITY", "End-game progression component created by Entropy Seed mechanics and used by advanced FinalTECH systems."),
            Map.entry("_FINALTECH_EQUIVALENT_CONCEPT", "End-game progression component created by Entropy Seed mechanics and used by advanced FinalTECH systems."),
            Map.entry("_FINALTECH_BEDROCK_CRAFT_TABLE", "Crafting station for Bedrock Craft Table recipes used by many FinalTECH machines."),
            Map.entry("_FINALTECH_MATRIX_CRAFTING_TABLE", "End-game crafting station for Matrix-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_ITEM_DISMANTLE_TABLE", "Breaks supported crafted items back down into component materials."),
            Map.entry("_FINALTECH_AUTO_ITEM_DISMANTLE_TABLE", "Automatically dismantles supported crafted items back into component materials."),
            Map.entry("_FINALTECH_CARD_OPERATION_TABLE", "Manipulates Copy and Storage Cards and crafts advanced card components such as Annular, Shell and Phony."),
            Map.entry("_FINALTECH_COBBLESTONE_FACTORY", "Turns any cobblestone stack placed in its storage slots into a full stack of 64."),
            Map.entry("_FINALTECH_CRUCIBLE", "Manual shortcut for Crucible recipes; use its menu to process matching recipes in batches."),
            Map.entry("_FINALTECH_BASIC_LOGIC_FACTORY", "Consumes Logic False, Logic True and a Bug to produce Entropy."),
            Map.entry("_FINALTECH_GRAVEL_CONVERSION", "Processes the same conversion recipes as Slimefun's Gold Pan."),
            Map.entry("_FINALTECH_SOUL_SAND_CONVERSION", "Processes the same conversion recipes as Slimefun's Nether Gold Pan."),
            Map.entry("_FINALTECH_LOGIC_TO_DIGITAL_CONVERSION", "Converts Logic False into Digital 0 and Logic True into Digital 1."),
            Map.entry("_FINALTECH_DIGITAL_EXTRACTION", "Consumes Logic False to randomly produce Digital 0-7, or Logic True to randomly produce Digital 8-15."),
            Map.entry("_FINALTECH_LIQUID_CARD_GENERATOR", "Randomly generates Water, Lava or Milk Cards for compatible FinalTECH recipes."),
            Map.entry("_FINALTECH_LOGIC_GENERATOR", "Randomly generates either a Logic False or Logic True token."),
            Map.entry("_FINALTECH_DIGITAL_GENERATOR", "Randomly generates Digital number tokens 1 through 4."),
            Map.entry("_FINALTECH_MATRIX_ITEM_DISMANTLE_TABLE", "Matrix-tier machine for dismantling supported items back into raw materials."),
            Map.entry("_FINALTECH_MATRIX_EXPANDED_CAPACITOR", "Matrix-tier capacitor that stores enormous amounts of energy in energy stacks."),
            Map.entry("_FINALTECH_ELECTRIC_REACTOR", "Legacy activation reactor: consumes nearby stored energy and catalysts to advance reactions that create high-tier FinalTECH items."),
            Map.entry("_FINALTECH_ENTROPY_CLEANER", "Right-click to toggle cleanup of the Justifiability and Equivalent Concept effects produced by Entropy Seeds."),
            Map.entry("_FINALTECH_STRING", "Plant it and, after a delay, it transforms into a random Slimefun item. Obtained through the Electric Reactor."),
            Map.entry("_FINALTECH_TROPHY_BALUGAQ", "Collectible contributor trophy. It has no machine or crafting function."),
            Map.entry("_FINALTECH_TROPHY_MEAWERFUL", "Collectible contributor trophy. It has no machine or crafting function."),
            Map.entry("_FINALTECH_TROPHY_QY", "Collectible contributor trophy. It has no machine or crafting function."),
            Map.entry("_FINALTECH_TROPHY_SHIXINZIA", "Collectible contributor trophy. It has no machine or crafting function."));

    private GuideItemLoreUtil() {
    }

    @Nonnull
    public static ItemStack createGuideIcon(@Nonnull SlimefunItem slimefunItem, boolean cheatMode) {
        ItemStack icon = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = new ArrayList<>();

        lore.addAll(getPurposeLore(slimefunItem));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + slimefunItem.getId());

        if (cheatMode) {
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-click: Give 1");
            lore.add(ChatColor.GREEN + "Right-click: Give a full stack");
            lore.add(ChatColor.YELLOW + "Shift-click: Bookmark");
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    @Nonnull
    public static List<String> getPurposeLore(@Nonnull SlimefunItem slimefunItem) {
        LanguageManager languageManager = FinalTechChanged.getLanguageManager();
        String id = slimefunItem.getId();

        // Explicit English guide text always wins when present.
        List<String> explicit = getStableLines(languageManager, "items", id, "guide-lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        // Prefer the lore that is actually attached to the registered item.
        // This is the closest match to what a player sees when holding the item,
        // and it also preserves descriptions assembled dynamically by item code.
        List<String> itemLore = deriveFromItemLore(slimefunItem.getItem());
        if (!itemLore.isEmpty()) {
            return itemLore;
        }

        // Some item classes expose already-resolved runtime information. Prefer
        // it because placeholders such as {1} have already been filled in.
        if (slimefunItem instanceof ShowInfoItem showInfoItem) {
            List<String> runtimeInfo = deriveFromResolvedInfo(showInfoItem.getInfos());
            if (!runtimeInfo.isEmpty()) {
                return runtimeInfo;
            }
        }

        // The original FinalTECH locales keep the real manual under nested
        // info sections. Surface those before considering decorative lore.
        List<String> configuredInfo = deriveFromConfiguredInfo(languageManager, id);
        if (!configuredInfo.isEmpty()) {
            return configuredInfo;
        }

        // Some simple items have useful ordinary lore. Reject the old one-line
        // flavor placeholders so they never replace an actual explanation.
        explicit = getStableLines(languageManager, "items", id, "lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        return wrapGray(fallbackPurpose(id, slimefunItem.getItemName()));
    }

    @Nonnull
    private static List<String> deriveFromItemLore(@Nonnull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return List.of();
        }

        List<String> source = meta.getLore();
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String line : source) {
            appendStableWrapped(result, line, null);
            if (result.size() >= MAX_PURPOSE_LINES) {
                break;
            }
        }
        return result;
    }

    @Nonnull
    private static List<String> deriveFromResolvedInfo(String[] infos) {
        if (infos == null || infos.length == 0) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        boolean collect = false;

        for (String line : infos) {
            if (!isUsableText(line, null)) {
                if (collect && !result.isEmpty()) {
                    break;
                }
                continue;
            }

            String plain = ChatColor.stripColor(line);
            if (plain == null) {
                continue;
            }
            String normalized = plain.trim().toLowerCase(Locale.ROOT);
            boolean heading = isPurposeHeading(normalized);

            if (heading) {
                collect = true;
                int colon = plain.indexOf(':');
                if (colon >= 0 && colon + 1 < plain.length()) {
                    appendWrappedGray(result, plain.substring(colon + 1).trim());
                }
                continue;
            }

            if (collect) {
                appendWrappedGray(result, plain);
                if (result.size() >= MAX_PURPOSE_LINES) {
                    break;
                }
            }
        }
        return result;
    }

    @Nonnull
    private static List<String> deriveFromConfiguredInfo(@Nonnull LanguageManager languageManager, @Nonnull String id) {
        List<String> firstUsefulSection = List.of();

        for (int i = 1; i <= 32; i++) {
            String section = String.valueOf(i);
            if (!languageManager.containPath("items", id, "info", section, "name")
                    || !languageManager.containPath("items", id, "info", section, "lore")) {
                continue;
            }

            List<String> lines = getStableLines(languageManager, "items", id, "info", section, "lore");
            if (lines.isEmpty()) {
                continue;
            }

            String heading = ChatColor.stripColor(languageManager.getString("items", id, "info", section, "name"));
            String normalizedHeading = heading == null ? "" : heading.trim().toLowerCase(Locale.ROOT);
            if (isPurposeHeading(normalizedHeading)) {
                return lines;
            }

            // If the original item has no Usage/Introduction/Mechanism section,
            // its first descriptive section is still preferable to a guess.
            if (firstUsefulSection.isEmpty()) {
                firstUsefulSection = lines;
            }
        }

        return firstUsefulSection;
    }

    private static boolean isPurposeHeading(@Nonnull String normalizedHeading) {
        return normalizedHeading.contains("usage")
                || normalizedHeading.contains("introduction")
                || normalizedHeading.contains("overview")
                || normalizedHeading.contains("mechanism")
                || normalizedHeading.contains("function")
                || normalizedHeading.contains("purpose")
                || normalizedHeading.contains("description")
                || normalizedHeading.contains("effect")
                || normalizedHeading.contains("operation");
    }

    @Nonnull
    private static List<String> getStableLines(@Nonnull LanguageManager languageManager, @Nonnull String... path) {
        if (!languageManager.containPath(path)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        List<String> configured = languageManager.getStringList(path);
        if (!configured.isEmpty()) {
            for (String line : configured) {
                appendStableWrapped(result, line, null);
                if (result.size() >= MAX_PURPOSE_LINES) {
                    break;
                }
            }
            return result;
        }

        String scalar = languageManager.getString(path);
        String rawPath = String.join(".", path);
        appendStableWrapped(result, scalar, rawPath);
        return result;
    }

    private static void appendStableWrapped(@Nonnull List<String> result, String value, String rawPath) {
        if (!isUsableText(value, rawPath)) {
            return;
        }
        appendWrappedGray(result, ChatColor.stripColor(value));
    }

    private static boolean isUsableText(String value, String rawPath) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String plain = ChatColor.stripColor(value);
        if (plain == null) {
            return false;
        }

        String trimmed = plain.trim();
        if (trimmed.isEmpty()
                || STRUCTURAL_PLACEHOLDER.matcher(trimmed).matches()
                || DYNAMIC_PLACEHOLDER.matcher(trimmed).find()
                || FLAVOR_ONLY.matcher(trimmed).matches()) {
            return false;
        }

        return rawPath == null || !trimmed.equals(rawPath);
    }

    private static void appendWrappedGray(@Nonnull List<String> result, String value) {
        if (value == null || value.isBlank() || result.size() >= MAX_PURPOSE_LINES) {
            return;
        }

        StringBuilder current = new StringBuilder();
        for (String word : value.trim().split("\\s+")) {
            if (current.length() > 0 && current.length() + 1 + word.length() > MAX_LINE_LENGTH) {
                result.add(ChatColor.GRAY + current.toString());
                current.setLength(0);
                if (result.size() >= MAX_PURPOSE_LINES) {
                    return;
                }
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }
        if (current.length() > 0 && result.size() < MAX_PURPOSE_LINES) {
            result.add(ChatColor.GRAY + current.toString());
        }
    }

    @Nonnull
    private static List<String> wrapGray(@Nonnull String value) {
        List<String> result = new ArrayList<>();
        appendWrappedGray(result, value);
        return result;
    }

    @Nonnull
    private static String fallbackPurpose(@Nonnull String id, @Nonnull String itemName) {
        String exact = EXACT_FALLBACKS.get(id);
        if (exact != null) {
            return exact;
        }

        if (id.equals("_FINALTECH_LOGIC_FALSE") || id.equals("_FINALTECH_LOGIC_TRUE")) {
            return "Boolean logic token used by FinalTECH logic machines, comparisons and conversions.";
        }
        if (id.startsWith("_FINALTECH_DIGITAL_")) {
            return "Numeric token used by FinalTECH digital logic, arithmetic and conversion machines.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_CHARGE_CARD_") || id.contains("MACHINE_CHARGE_CARD")) {
            return "Consumable card that adds energy to a supported FinalTECH machine.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACCELERATE_CARD_") || id.contains("MACHINE_ACCELERATE_CARD")) {
            return "Consumable card that immediately advances a supported machine's current operation.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACTIVATE_CARD_") || id.contains("MACHINE_ACTIVATE_CARD")) {
            return "Consumable card that charges and immediately runs supported machine cycles.";
        }
        if (id.startsWith("_FINALTECH_ENERGY_CARD_")) {
            return "Portable energy card created and used by FinalTECH Energy Tables to move stored power.";
        }
        if (id.contains("STORAGE_UNIT")) {
            return "Cargo-compatible item storage; this variant changes how stored items are divided, limited or accessed.";
        }
        if (id.contains("ACCESSOR")) {
            return "Remotely reads or interacts with compatible FinalTECH inventories and machines.";
        }
        if (id.contains("TRANSPORTER") || id.endsWith("_TRANSFER")) {
            return "Moves items between compatible FinalTECH inventories and cargo systems.";
        }
        if (id.contains("LOGIC_COMPARATOR")) {
            return "Compares configured inputs and outputs TRUE/FALSE logic for FinalTECH automation.";
        }
        if (id.startsWith("_FINALTECH_MANUAL_")) {
            return "Manual shortcut for " + stripPrefix(itemName, "Manual ")
                    + " recipes. Use its menu to craft matching recipes; click type controls batch size.";
        }
        if (id.startsWith("_FINALTECH_ADVANCED_")) {
            return "Automatically processes " + stripPrefix(itemName, "Advanced ")
                    + " recipes using FinalTECH's high-throughput machine system; supports Quantity Modules and recipe locking.";
        }
        if (id.startsWith("_FINALTECH_MATRIX_")) {
            return "End-game Matrix-tier technology used by FinalTECH's highest-tier crafting, storage, and automation systems.";
        }
        if (id.endsWith("_CAPACITOR")) {
            return "Stores energy for the Slimefun/FinalTECH power network.";
        }
        if (id.endsWith("_CONVERSION")) {
            return "Converts supported FinalTECH resources into another usable form.";
        }
        if (id.endsWith("_EXTRACTION")) {
            return "Extracts useful FinalTECH resources from supported inputs.";
        }
        if (id.endsWith("_GENERATOR")) {
            return "Generates a resource or electrical power used by FinalTECH systems.";
        }
        if (id.contains("CARD")) {
            return "FinalTECH card used by compatible machines or automation systems.";
        }
        if (id.contains("MODULE")) {
            return "Upgrade component used by compatible FinalTECH machines to change efficiency or capacity.";
        }

        String plainName = ChatColor.stripColor(itemName);
        if (plainName == null || plainName.isBlank()) {
            plainName = "This item";
        }
        return plainName + " is used by FinalTECH's crafting, progression, or automation systems.";
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
