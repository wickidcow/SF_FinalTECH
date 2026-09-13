from pathlib import Path


def replace_once(path, old, new, label):
    p = Path(path)
    text = p.read_text(encoding='utf-8')
    if old not in text:
        raise SystemExit(f'{label}: expected source block not found in {path}')
    text = text.replace(old, new, 1)
    p.write_text(text, encoding='utf-8')


# Main group: make the custom FinalTECH folder visible in cheat mode and never
# hard-code the back action to survival mode.
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/MainItemGroup.java',
    '        return layout.equals(SlimefunGuideMode.SURVIVAL_MODE) && this.page == 1;\n',
    '        return this.page == 1;\n',
    'MainItemGroup cheat visibility')
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/MainItemGroup.java',
    '                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));\n',
    '                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(slimefunGuideMode));\n',
    'MainItemGroup back mode')

# Sub group imports and back-mode handling.
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java',
    'import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;\nimport io.github.thebusybiscuit.slimefun4.implementation.Slimefun;\n',
    'import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;\nimport io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;\nimport io.github.thebusybiscuit.slimefun4.implementation.Slimefun;\n',
    'SubFlexItemGroup multiblock import')
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java',
    'import io.taraxacum.finaltech.FinalTechChanged;\nimport io.taraxacum.libs.plugin.util.ItemStackUtil;\n',
    'import io.taraxacum.finaltech.FinalTechChanged;\nimport io.taraxacum.finaltech.util.GuideItemLoreUtil;\nimport io.taraxacum.libs.plugin.util.ItemStackUtil;\n',
    'SubFlexItemGroup lore helper import')
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java',
    '                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));\n',
    '                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(slimefunGuideMode));\n',
    'SubFlexItemGroup back mode')

old_item_block = '''                    Research research = slimefunItem.getResearch();
                    if (playerProfile.hasUnlocked(research)) {
                        ItemStack itemStack = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
                        ItemStackUtil.addLoreToFirst(itemStack, "§7" + slimefunItem.getId());
                        chestMenu.addItem(MAIN_CONTENT_L[i][j], ItemStackUtil.cleanItem(itemStack));
                        chestMenu.addMenuClickHandler(MAIN_CONTENT_L[i][j], (p, slot, item, action) -> {
                            RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, slimefunItem.getItem());
                            if (recipeItemGroup != null) {
                                Bukkit.getScheduler().runTask(JAVA_PLUGIN, () -> recipeItemGroup.open(player, playerProfile, slimefunGuideMode));
                            }
                            return false;
                        });
                    } else {
'''
new_item_block = '''                    Research research = slimefunItem.getResearch();
                    if (slimefunGuideMode == SlimefunGuideMode.CHEAT_MODE) {
                        ItemStack itemStack = GuideItemLoreUtil.createGuideIcon(slimefunItem, true);
                        chestMenu.addItem(MAIN_CONTENT_L[i][j], ItemStackUtil.cleanItem(itemStack));
                        chestMenu.addMenuClickHandler(MAIN_CONTENT_L[i][j], (p, slot, item, action) -> {
                            if (!p.hasPermission("slimefun.cheat.items")) {
                                Slimefun.getLocalization().sendMessage(p, "messages.no-permission", true);
                            } else if (slimefunItem instanceof MultiBlockMachine) {
                                Slimefun.getLocalization().sendMessage(p, "guide.cheat.no-multiblocks");
                            } else {
                                ItemStack clonedItem = slimefunItem.getItem().clone();
                                if (action.isShiftClicked()) {
                                    clonedItem.setAmount(clonedItem.getMaxStackSize());
                                }
                                p.getInventory().addItem(clonedItem);
                            }
                            return false;
                        });
                    } else if (research == null || playerProfile.hasUnlocked(research)) {
                        ItemStack itemStack = GuideItemLoreUtil.createGuideIcon(slimefunItem, false);
                        chestMenu.addItem(MAIN_CONTENT_L[i][j], ItemStackUtil.cleanItem(itemStack));
                        chestMenu.addMenuClickHandler(MAIN_CONTENT_L[i][j], (p, slot, item, action) -> {
                            RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, slimefunItem.getItem());
                            if (recipeItemGroup != null) {
                                Bukkit.getScheduler().runTask(JAVA_PLUGIN, () -> recipeItemGroup.open(player, playerProfile, slimefunGuideMode));
                            }
                            return false;
                        });
                    } else {
'''
replace_once(
    'src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java',
    old_item_block,
    new_item_block,
    'SubFlexItemGroup item click behavior')

# Targeted language self-heal for the old/dead-looking Wiki/Parameters helper.
replace_once(
    'src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java',
    'import org.bukkit.Location;\nimport org.bukkit.World;\n',
    'import org.bukkit.ChatColor;\nimport org.bukkit.Location;\nimport org.bukkit.World;\n',
    'SetupUtil ChatColor import')
replace_once(
    'src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java',
    '''        });
    }

    private static void setupEnchantment() {
''',
    '''        });

        setupGuideHelperLanguage(languageManager);
    }

    private static void setupGuideHelperLanguage(@Nonnull LanguageManager languageManager) {
        String currentName = languageManager.containPath("helper", "ICON", "wiki-icon", "name")
                ? languageManager.getString("helper", "ICON", "wiki-icon", "name")
                : "";
        String plainName = ChatColor.stripColor(currentName);
        if (plainName == null
                || plainName.isBlank()
                || plainName.equalsIgnoreCase("Parameters")
                || currentName.equals("helper.ICON.wiki-icon.name")) {
            languageManager.setValue("{color:stress}Item Information", "helper", "ICON", "wiki-icon", "name");
        }

        if (!languageManager.containPath("helper", "ICON", "wiki-icon", "lore")) {
            languageManager.setValue(List.of(
                    "§7{1}",
                    "{color:normal}Research: {color:negative}{2}",
                    "{color:normal}Addon: {color:initiative}{3}",
                    "{color:normal}EE Input Value: {color:number}{4}",
                    "{color:normal}EE Output Value: {color:number}{5}"),
                    "helper", "ICON", "wiki-icon", "lore");
        }
    }

    private static void setupEnchantment() {
''',
    'SetupUtil guide helper self-heal')

# Bundled English label: this is an information panel, not a web link.
replace_once(
    'src/main/resources/language/en-US.yml',
    "      name: '{color:stress}Parameters'\n",
    "      name: '{color:stress}Item Information'\n",
    'English Item Information label')

print('Applied FinalTECH item-purpose, helper-label and cheat-guide cleanup.')
