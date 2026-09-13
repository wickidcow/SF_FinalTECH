package io.taraxacum.finaltech.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.group.MainItemGroup;
import io.taraxacum.finaltech.core.group.SubFlexItemGroup;
import io.taraxacum.libs.plugin.dto.LanguageManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import javax.annotation.Nonnull;
import java.util.List;

public class ConfigUtil {
    @Nonnull
    public static String getStatusMenuName(@Nonnull LanguageManager languageManager, @Nonnull SlimefunItem slimefunItem, String... strings) {
        return languageManager.replaceString(languageManager.getString("items", slimefunItem.getId(), "status", "name"), strings);
    }

    @Nonnull
    public static String getStatusMenuName(@Nonnull LanguageManager languageManager, @Nonnull String id, String... strings) {
        return languageManager.replaceString(languageManager.getString("items", id, "status", "name"), strings);
    }

    @Nonnull
    public static String[] getStatusMenuLore(@Nonnull LanguageManager languageManager, @Nonnull SlimefunItem slimefunItem, String... strings) {
        return languageManager.replaceStringArray(languageManager.getStringArray("items", slimefunItem.getId(), "status", "lore"), strings);
    }

    @Nonnull
    public static String[] getStatusMenuLore(@Nonnull LanguageManager languageManager, @Nonnull String id, String... strings) {
        return languageManager.replaceStringArray(languageManager.getStringArray("items", id, "status", "lore"), strings);
    }

    @Nonnull
    public static SlimefunItemStack getSlimefunItemStack(@Nonnull LanguageManager languageManager, @Nonnull String id, @Nonnull Material defaultMaterial, @Nonnull String defaultName) {
        Material material = defaultMaterial;
        if (languageManager.containPath("categories", id, "material")) {
            material = Material.getMaterial(languageManager.getString("categories", id, "material"));
            material = material == null ? defaultMaterial : material;
        }
        String name = languageManager.containPath("items", id, "name") ? languageManager.getString("items", id, "name") : defaultName;
        return new SlimefunItemStack(id, material, name, languageManager.getStringArray("items", id, "lore"));
    }

    @Nonnull
    public static SlimefunItemStack getSlimefunItemStack(@Nonnull LanguageManager languageManager, @Nonnull String id, @Nonnull String texture, @Nonnull String defaultName) {
        String name = languageManager.containPath("items", id, "name") ? languageManager.getString("items", id, "name") : defaultName;
        try {
            return new SlimefunItemStack(id, texture, name, languageManager.getStringArray("items", id, "lore"));
        } catch (Exception e) {
            e.printStackTrace();
            return new SlimefunItemStack(id, Material.STONE, name, languageManager.getStringArray("items", id, "lore"));
        }
    }

    @Nonnull
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

    @Nonnull
    public static <T> T getOrDefaultItemSetting(@Nonnull T defaultValue, @Nonnull SlimefunItem slimefunItem, @Nonnull String... path) {
        return FinalTechChanged.getItemManager().getOrDefault(defaultValue, JavaUtil.addToFirst(slimefunItem.getId(), path));
    }

    @Nonnull
    public static <T> T getOrDefaultItemSetting(@Nonnull T defaultValue, @Nonnull String id, @Nonnull String... path) {
        return FinalTechChanged.getItemManager().getOrDefault(defaultValue, JavaUtil.addToFirst(id, path));
    }

    @Nonnull
    public static List<String> getItemStringList(@Nonnull SlimefunItem slimefunItem, @Nonnull String... path) {
        return FinalTechChanged.getItemManager().getStringList(JavaUtil.addToFirst(slimefunItem.getId(), path));
    }

    @Nonnull
    public static List<String> getItemStringList(@Nonnull String id, @Nonnull String... path) {
        return FinalTechChanged.getItemManager().getStringList(JavaUtil.addToFirst(id, path));
    }

    @Nonnull
    public static <T> T getOrDefaultConfigSetting(@Nonnull T defaultValue, @Nonnull String... path) {
        return FinalTechChanged.getConfigManager().getOrDefault(defaultValue, path);
    }

    @Nonnull
    public static <T> T getOrDefaultValueSetting(@Nonnull T defaultValue, @Nonnull String... path) {
        return FinalTechChanged.getValueManager().getOrDefault(defaultValue, path);
    }

}
