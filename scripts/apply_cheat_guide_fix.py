from pathlib import Path

main_path = Path('src/main/java/io/taraxacum/finaltech/core/group/MainItemGroup.java')
sub_path = Path('src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java')

main = main_path.read_text(encoding='utf-8')
old = 'guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));'
new = 'guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(slimefunGuideMode));'
if old not in main:
    raise SystemExit('MainItemGroup survival back-handler was not found')
main = main.replace(old, new)
main_path.write_text(main, encoding='utf-8')

sub = sub_path.read_text(encoding='utf-8')

old_import = 'import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;\n'
new_import = old_import + 'import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;\n'
if new_import not in sub:
    if old_import not in sub:
        raise SystemExit('SlimefunGuideMode import was not found')
    sub = sub.replace(old_import, new_import, 1)

old_back = 'guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));'
new_back = 'guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(slimefunGuideMode));'
if old_back not in sub:
    raise SystemExit('SubFlexItemGroup survival back-handler was not found')
sub = sub.replace(old_back, new_back, 1)

old_block = '''                    Research research = slimefunItem.getResearch();
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
new_block = '''                    Research research = slimefunItem.getResearch();
                    boolean cheatMode = slimefunGuideMode == SlimefunGuideMode.CHEAT_MODE;
                    if (cheatMode || research == null || playerProfile.hasUnlocked(research)) {
                        ItemStack itemStack = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
                        ItemStackUtil.addLoreToFirst(itemStack, "§7" + slimefunItem.getId());
                        chestMenu.addItem(MAIN_CONTENT_L[i][j], ItemStackUtil.cleanItem(itemStack));
                        chestMenu.addMenuClickHandler(MAIN_CONTENT_L[i][j], (p, slot, item, action) -> {
                            if (cheatMode) {
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
                            } else {
                                RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, slimefunItem.getItem());
                                if (recipeItemGroup != null) {
                                    Bukkit.getScheduler().runTask(JAVA_PLUGIN, () -> recipeItemGroup.open(player, playerProfile, slimefunGuideMode));
                                }
                            }
                            return false;
                        });
                    } else {
'''
if old_block not in sub:
    raise SystemExit('SubFlexItemGroup unlocked item block was not found')
sub = sub.replace(old_block, new_block, 1)

sub_path.write_text(sub, encoding='utf-8')
