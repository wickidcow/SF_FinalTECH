from pathlib import Path

sub_path = Path('src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java')
setup_path = Path('src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java')
yaml_path = Path('src/main/resources/language/en-US.yml')

sub = sub_path.read_text(encoding='utf-8')
import_anchor = 'import io.taraxacum.finaltech.FinalTechChanged;\n'
import_line = 'import io.taraxacum.finaltech.util.GuideItemLoreUtil;\n'
if import_line not in sub:
    if import_anchor not in sub:
        raise SystemExit('SubFlexItemGroup import anchor not found')
    sub = sub.replace(import_anchor, import_anchor + import_line, 1)

old_render = '''                        ItemStack itemStack = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
                        ItemStackUtil.addLoreToFirst(itemStack, "§7" + slimefunItem.getId());
'''
new_render = '''                        ItemStack itemStack = GuideItemLoreUtil.createGuideIcon(slimefunItem, cheatMode);
'''
if old_render not in sub:
    raise SystemExit('SubFlexItemGroup item render block not found')
sub = sub.replace(old_render, new_render, 1)
sub_path.write_text(sub, encoding='utf-8')

setup = setup_path.read_text(encoding='utf-8')
setup = setup.replace(
    'import io.taraxacum.finaltech.FinalTechChanged;\nimport io.taraxacum.finaltech.FinalTechChanged;\n',
    'import io.taraxacum.finaltech.FinalTechChanged;\n',
    1,
)
if 'import org.bukkit.ChatColor;\n' not in setup:
    anchor = 'import org.bukkit.Location;\n'
    if anchor not in setup:
        raise SystemExit('SetupUtil Bukkit import anchor not found')
    setup = setup.replace(anchor, 'import org.bukkit.ChatColor;\n' + anchor, 1)

call_anchor = '''        });
    }

    private static void setupEnchantment() {
'''
call_replacement = '''        });

        setupGuideHelperLanguage(languageManager);
    }

    private static void setupGuideHelperLanguage(@Nonnull LanguageManager languageManager) {
        String path = "helper.ICON.wiki-icon.name";
        String currentName = languageManager.containPath("helper", "ICON", "wiki-icon", "name")
                ? languageManager.getString("helper", "ICON", "wiki-icon", "name")
                : "";
        String plainName = ChatColor.stripColor(currentName);

        if (plainName == null
                || plainName.isBlank()
                || plainName.equalsIgnoreCase("Parameters")
                || currentName.equals(path)) {
            languageManager.setValue("{color:stress}Item Info", "helper", "ICON", "wiki-icon", "name");
        }
    }

    private static void setupEnchantment() {
'''
if 'setupGuideHelperLanguage(languageManager);' not in setup:
    if call_anchor not in setup:
        raise SystemExit('SetupUtil language setup anchor not found')
    setup = setup.replace(call_anchor, call_replacement, 1)
setup_path.write_text(setup, encoding='utf-8')

yaml_text = yaml_path.read_text(encoding='utf-8')
old_names = [
    "      name: '{color:stress}Parameters'",
    "      name: '{color:positive}Parameters'",
]
new_name = "      name: '{color:stress}Item Info'"
for old_name in old_names:
    if old_name in yaml_text:
        yaml_text = yaml_text.replace(old_name, new_name, 1)
        break
else:
    if new_name not in yaml_text:
        raise SystemExit('wiki icon English name was not found')
yaml_path.write_text(yaml_text, encoding='utf-8')
