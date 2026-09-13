package io.taraxacum.finaltech.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.libs.plugin.dto.LanguageManager;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.plugin.util.TextUtil;
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
 * <p>FinalTECH already contains a lot of useful Usage/Mechanism information,
 * but historically that information was hidden inside the recipe page. This
 * helper promotes the useful part into the category browser and supplies
 * conservative fallbacks for simple materials/components.</p>
 */
public final class GuideItemLoreUtil {
    private static final int MAX_PURPOSE_LINES = 3;
    private static final Pattern DYNAMIC_PLACEHOLDER = Pattern.compile("\\{\\d+}");

    private static final Map<String, String> EXACT_FALLBACKS = Map.ofEntries(
            Map.entry("_FINALTECH_GEARWHEEL", "Crafting component used throughout FinalTECH machines and technology."),
            Map.entry("_FINALTECH_UNORDERED_DUST", "Intermediate material refined into Ordered Dust for FinalTECH crafting."),
            Map.entry("_FINALTECH_ORDERED_DUST", "Refined crafting material used in higher-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_BUG", "Special progression material used by FinalTECH crafting and exchange systems."),
            Map.entry("_FINALTECH_ENTROPY", "Core resource used by FinalTECH logic and advanced crafting systems."),
            Map.entry("_FINALTECH_ETHER", "Advanced resource used in higher-tier FinalTECH recipes."),
            Map.entry("_FINALTECH_ANNULAR", "Advanced crafting component used by FinalTECH card and machine recipes."),
            Map.entry("_FINALTECH_SINGULARITY", "High-tier component used by FinalTECH serialization and end-game recipes."),
            Map.entry("_FINALTECH_SPIROCHETE", "High-tier component used by FinalTECH serialization and end-game recipes."),
            Map.entry("_FINALTECH_PHONY", "High-tier component used by serialization and card-operation recipes."),
            Map.entry("_FINALTECH_JUSTIFIABILITY", "End-game crafting component used by advanced FinalTECH technology."),
            Map.entry("_FINALTECH_EQUIVALENT_CONCEPT", "End-game crafting component used by advanced FinalTECH technology."),
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

        List<String> explicit = getStableLines(languageManager, "items", id, "guide-lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        // Prefer runtime information because placeholders such as {1} are already resolved there.
        if (slimefunItem instanceof ShowInfoItem showInfoItem) {
            List<String> runtimeInfo = deriveFromResolvedInfo(showInfoItem.getInfos());
            if (!runtimeInfo.isEmpty()) {
                return runtimeInfo;
            }
        }

        explicit = getStableLines(languageManager, "items", id, "lore");
        if (!explicit.isEmpty()) {
            return explicit;
        }

        List<String> configuredInfo = deriveFromConfiguredInfo(languageManager, id);
        if (!configuredInfo.isEmpty()) {
            return configuredInfo;
        }

        return wrap(ChatColor.GRAY + fallbackPurpose(id, slimefunItem.getItemName()));
    }

    @Nonnull
    private static List<String> deriveFromResolvedInfo(@Nonnull Map<String, String> infos) {
        for (String preferred : List.of("purpose", "usage", "function", "mechanism", "description")) {
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String heading = ChatColor.stripColor(entry.getKey());
                if (heading != null && heading.toLowerCase(Locale.ROOT).contains(preferred)) {
                    List<String> lines = stableWrappedValue(entry.getValue());
                    if (!lines.isEmpty()) {
                        return lines;
                    }
                }
            }
        }
        return List.of();
    }

    @Nonnull
    private static List<String> deriveFromConfiguredInfo(@Nonnull LanguageManager languageManager, @Nonnull String id) {
        for (int i = 1; i <= 16; i++) {
            String section = String.valueOf(i);
            if (!languageManager.containPath("items", id, "info", section, "name")
                    || !languageManager.containPath("items", id, "info", section, "lore")) {
                continue;
            }

            String heading = ChatColor.stripColor(languageManager.getString("items", id, "info", section, "name"));
            String normalizedHeading = heading == null ? "" : heading.toLowerCase(Locale.ROOT);
            if (!normalizedHeading.contains("usage")
                    && !normalizedHeading.contains("mechanism")
                    && !normalizedHeading.contains("function")
                    && !normalizedHeading.contains("purpose")
                    && !normalizedHeading.contains("description")) {
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

        List<String> configured = languageManager.getStringList(path);
        if (!configured.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (String line : configured) {
                appendStableWrapped(result, line);
                if (result.size() >= MAX_PURPOSE_LINES) {
                    break;
                }
            }
            return result;
        }

        String scalar = languageManager.getString(path);
        String rawPath = String.join(".", path);
        if (scalar.equals(rawPath)) {
            return List.of();
        }
        return stableWrappedValue(scalar);
    }

    @Nonnull
    private static List<String> stableWrappedValue(String value) {
        List<String> result = new ArrayList<>();
        appendStableWrapped(result, value);
        return result;
    }

    private static void appendStableWrapped(@Nonnull List<String> result, String value) {
        if (value == null || value.isBlank() || DYNAMIC_PLACEHOLDER.matcher(value).find()) {
            return;
        }
        for (String line : TextUtil.getSmallString(value, 40)) {
            if (!line.isBlank()) {
                result.add(ChatColor.GRAY + line);
                if (result.size() >= MAX_PURPOSE_LINES) {
                    return;
                }
            }
        }
    }

    @Nonnull
    private static List<String> wrap(@Nonnull String value) {
        List<String> result = new ArrayList<>();
        for (String line : TextUtil.getSmallString(value, 40)) {
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
            return "Boolean logic token used by FinalTECH logic machines and conversions.";
        }
        if (id.startsWith("_FINALTECH_DIGITAL_")) {
            return "Numeric token used by FinalTECH digital logic, arithmetic and conversion machines.";
        }
        if (id.startsWith("_FINALTECH_MACHINE_ACCELERATE_CARD_") || id.contains("MACHINE_ACCELERATE_CARD")) {
            return "Card used to advance or accelerate supported machine operation.";
        }
        if (id.contains("STORAGE_UNIT")) {
            return "Cargo-compatible item storage; this variant changes how stored items are organized or accessed.";
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
