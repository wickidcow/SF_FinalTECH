from pathlib import Path

MAIN_GROUP = Path("src/main/java/io/taraxacum/finaltech/core/group/MainItemGroup.java")
SUB_GROUP = Path("src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java")
SETUP = Path("src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java")


def insert_preserved_lore_override(path: Path, anchor: str):
    text = path.read_text(encoding="utf-8")
    method = '''
    @Override
    public @Nonnull ItemStack getItem(@Nonnull Player player) {
        ItemStack displayItem = super.getItem(player);
        if (!this.item.hasItemMeta()) {
            return displayItem;
        }

        List<String> categoryLore = this.item.getItemMeta().getLore();
        if (categoryLore == null || categoryLore.isEmpty()) {
            return displayItem;
        }

        var displayMeta = displayItem.getItemMeta();
        List<String> combinedLore = new ArrayList<>(categoryLore);
        List<String> actionLore = displayMeta.getLore();
        if (actionLore != null) {
            combinedLore.addAll(actionLore);
        }
        displayMeta.setLore(combinedLore);
        displayItem.setItemMeta(displayMeta);
        return displayItem;
    }
'''
    if method.strip() in text:
        return
    if anchor not in text:
        raise SystemExit(f"Expected isVisible anchor not found in {path}")
    text = text.replace(anchor, anchor + method, 1)
    path.write_text(text, encoding="utf-8")


main_anchor = '''    @Override
    public boolean isVisible(@Nonnull Player p, @Nonnull PlayerProfile profile, @Nonnull SlimefunGuideMode layout) {
        return layout.equals(SlimefunGuideMode.SURVIVAL_MODE) && this.page == 1;
    }
'''
sub_anchor = '''    @Override
    public boolean isVisible(@Nonnull Player player, @Nonnull PlayerProfile playerProfile, @Nonnull SlimefunGuideMode slimefunGuideMode) {
        return false;
    }
'''

insert_preserved_lore_override(MAIN_GROUP, main_anchor)
insert_preserved_lore_override(SUB_GROUP, sub_anchor)

setup = SETUP.read_text(encoding="utf-8")
old_tier = "        FinalTechMenus.MAIN_ITEM_GROUP.setTier(0);"
new_tier = "        FinalTechMenus.MAIN_ITEM_GROUP.setTier(3);"
if old_tier in setup:
    setup = setup.replace(old_tier, new_tier, 1)
elif new_tier not in setup:
    raise SystemExit("Expected FinalTECH Guide tier assignment was not found")
SETUP.write_text(setup, encoding="utf-8")

print("FinalTECH category lore will be preserved in Guide icons; main Guide tier normalized to 3.")
