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
 * Builds concise player-facing purpose text for FinalTECH items in the Guide.
 *
 * <p>The original FinalTECH language files keep most gameplay explanations in
 * {@code items.<id>.info.<n>.lore}, while {@code items.<id>.lore} is frequently
 * empty or only contains flavor text. This helper promotes the useful original
 * Usage/Mechanism information into the category browser and only falls back to
 * generic text when the language file genuinely has no useful explanation.</p>
 */
public final class GuideItemLoreUtil {
    private static final int MAX_PURPOSE_LINES = 3;
    private static final int MAX_LINE_LENGTH = 42;
    private static final Pattern DYNAMIC_PLACEHOLDER = Pattern.compile("\\{\\d+}");
    private static final Pattern STRUCTURAL_PLACEHOLDER = Pattern.compile("^(?:\\[\\s*]|\\{\\s*}|null|~)$", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> EXACT_FALLBACKS = Map.ofEntries(
            Map.entry("_FINALTECH_GEARWHEEL", "Crafting component used throughout FinalTECH machines and technology."),
            Map.entry("_FINALTECH_UNORDERED_DUST", "Intermediate material refined into Ordered Dust for FinalTECH crafting."),
            Map.entry("_FINALTECH_ORDERED_DUST", "Refined crafting material used in higher-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_BUG", "Special progression material used by FinalTECH crafting and exchange systems."),
            Map.entry("_FINALTECH_ENTROPY", "Core resource used by FinalTECH logic and advanced crafting systems."),
            Map.entry("_FINALTECH_ETHER", "GEO resource mined by the Ether Miner and used in advanced FinalTECH recipes."),
            Map.entry("_FINALTECH_ANNULAR", "Advanced crafting component produced by the Card Operation Table."),
            Map.entry("_FINALTECH_SINGULARITY", "High-tier component produced by item serialization machines."),
            Map.entry("_FINALTECH_SPIROCHETE", "High-tier component produced by item serialization machines."),
            Map.entry("_FINALTECH_PHONY", "High-tier component produced by serialization and card-operation machines."),
            Map.entry("_FINALTECH_JUSTIFIABILITY", "End-game progression component obtained from an Entropy Seed."),
            Map.entry("_FINALTECH_EQUIVALENT_CONCEPT", "End-game progression component obtained from an Entropy Seed."),
            Map.entry("_FINALTECH_BEDROCK_CRAFT_TABLE", "Crafting station for Bedrock Craft Table recipes used by many FinalTECH machines."),
            Map.entry("_FINALTECH_MATRIX_CRAFTING_TABLE", "End-game crafting station for Matrix-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_ITEM_DISMANTLE_TABLE", "Breaks supported items back down into component materials."),
            Map.entry("_FINALTECH_AUTO_ITEM_DISMANTLE_TABLE", "Automates dismantling supported items back into components."),
            Map.entry("_FINALTECH_CARD_OPERATION_TABLE", "Creates and modifies FinalTECH cards and card components."),
            Map.entry("_FINALTECH_COBBLESTONE_FACTORY", "Produces cobblestone automatically for resource-processing setups."),
            Map.entry("_FINALTECH_CRUCIBLE", "Manual processing station for Crucible recipes."),
            Map.entry("_FINALTECH_BASIC_LOGIC_FACTORY", "Produces and processes basic logic resources used by FinalTECH."),
            Map.entry("_FINALTECH_LOGIC_TO_DIGITAL_CONVERSION", "Converts TRUE/FALSE logic into digital values used by FinalTECH."),
            Map.entry("_FINALTECH_DIGITAL_EXTRACTION", "Extracts digital number tokens from FinalTECH logic values."),
            Map.entry("_FINALTECH_LIQUID_CARD_GENERATOR", "Generates liquid cards for supported FinalTECH machines."),
            Map.entry("_FINALTECH_LOGIC_GENERATOR", "Generates TRUE/FALSE logic tokens for FinalTECH logic systems."),
            Map.entry("_FINALTECH_DIGITAL_GENERATOR", "Generates digital number tokens for FinalTECH logic systems."),
            Map.entry("_FINALTECH_MATRIX_ITEM_DISMANTLE_TABLE", "Matrix-tier machine for high-end item dismantling."),
            Map.entry("_FINALTECH_MATRIX_EXPANDED_CAPACITOR", "Matrix-tier capacitor for extremely large energy storage."));

    private GuideItemLoreUtil() {
    }

    @Nonnull
    public static ItemStack createGuideIcon(@Nonnull SlimefunItem slimefunItem, boolean cheatMode) {
        ItemStack icon = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GOLD + "Purpose");
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

        // Some item classes expose already-resolved runtime information. Prefer
        // a Usage/Mechanism section because dynamic values are already filled in.
        if (slimefunItem instanceof ShowInfoItem showInfoItem) {
            List<String> runtimeInfo = deriveFromResolvedInfo(showInfoItem.getInfos());
            if (!runtimeInfo.isEmpty()) {
                return runtimeInfo;
            }
        }

        // The original Chinese FinalTECH files put the actual instructions in
        // the nested "info" sections. English translations preserve most of
        // that data, so surface it before decorative item lore.
        List<String> configuredInfo = deriveFromConfiguredInfo(languageManager, id);
        if (!configuredInfo.isEmpty()) {
            return configuredInfo;
        }

        explicit = getStableLines(languageManager, "items", id, "lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        return wrapGray(fallbackPurpose(id, slimefunItem.getItemName()));
    }

    @Nonnull
    private static List<String> deriveFromResolvedInfo(@Nonnull String[] infos) {
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

            // If the original item has no Usage/Mechanism section, its first
            // descriptive section is still more informative than a generic
            // guess. This covers Production method, Obtaining method, etc.
            if (firstUsefulSection.isEmpty()) {
                firstUsefulSection = lines;
            }
        }

        return firstUsefulSection;
    }

    private static boolean isPurposeHeading(@Nonnull String normalizedHeading) {
        return normalizedHeading.contains("usage")
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
                || DYNAMIC_PLACEHOLDER.matcher(trimmed).find()) {
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
            return "Boolean logic token used by FinalTECH logic machines and conversions.";
        }
        if (id.startsWith("_FINALTECH_DIGITAL_")) {
            return "Numeric token used by FinalTECH digital logic, arithmetic and conversion machines.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_CHARGE_CARD_") || id.contains("MACHINE_CHARGE_CARD")) {
            return "Consumable card used to add energy to a supported FinalTECH machine.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACCELERATE_CARD_") || id.contains("MACHINE_ACCELERATE_CARD")) {
            return "Consumable card used to advance a supported machine's current operation.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACTIVATE_CARD_") || id.contains("MACHINE_ACTIVATE_CARD")) {
            return "Consumable card used to trigger a supported FinalTECH machine.";
        }
        if (id.startsWith("_FINALTECH_ENERGY_CARD_")) {
            return "Portable energy card used by compatible FinalTECH energy machines.";
        }
        if (id.contains("STORAGE_UNIT")) {
            return "Cargo-compatible item storage; this variant changes how stored items are organized or accessed.";
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
            return "Manual processing station for " + stripPrefix(itemName, "Manual ") + " recipes.";
        }
        if (id.startsWith("_FINALTECH_ADVANCED_")) {
            return "Advanced FinalTECH machine or component for higher-tier automation.";
        }
        if (id.startsWith("_FINALTECH_MATRIX_")) {
            return "End-game Matrix-tier technology for advanced FinalTECH automation.";
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
            return "Generates a resource or power used by FinalTECH systems.";
        }
        if (id.contains("CARD")) {
            return "FinalTECH card used by compatible machines or automation systems.";
        }
        if (id.contains("MODULE")) {
            return "Upgrade component used by compatible FinalTECH machines.";
        }

        return "Used by FinalTECH crafting, progression or automation; open it for recipe and item details.";
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
