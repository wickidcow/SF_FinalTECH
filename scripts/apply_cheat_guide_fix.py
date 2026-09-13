from pathlib import Path

sub_path = Path('src/main/java/io/taraxacum/finaltech/core/group/SubFlexItemGroup.java')
sub = sub_path.read_text(encoding='utf-8')

# FinalTECH still compiles against the upstream RC-33 API. Slimefun Legacy's
# enhanced-guide bookmark classes are newer internal classes, so reference them
# reflectively at runtime instead of making them a hard compile-time dependency.
sub = sub.replace(
    'import io.github.thebusybiscuit.slimefun4.implementation.guide.enhanced.LegacyGuideBookmarks;\n',
    '',
)
sub = sub.replace(
    'import io.github.thebusybiscuit.slimefun4.implementation.guide.enhanced.LegacyGuideSettings;\n',
    '',
)

sub = sub.replace('LegacyGuideSettings.get().hasBookmarks()', 'hasLegacyBookmarks()')
sub = sub.replace(
    'LegacyGuideBookmarks.get().contains(player.getUniqueId(), slimefunItem.getId())',
    'isBookmarked(player, slimefunItem)',
)

old_toggle = '''    private void toggleBookmark(@Nonnull Player player, @Nonnull SlimefunItem slimefunItem) {
        boolean added = LegacyGuideBookmarks.get().toggle(player.getUniqueId(), slimefunItem.getId());
        player.sendMessage(
                added
                        ? ChatColor.GOLD + "★ Added " + ChatColor.WHITE + ChatColor.stripColor(slimefunItem.getItemName())
                                + ChatColor.GOLD + " to your bookmarks."
                        : ChatColor.YELLOW + "Removed " + ChatColor.WHITE + ChatColor.stripColor(slimefunItem.getItemName())
                                + ChatColor.YELLOW + " from your bookmarks.");
    }
'''
new_toggle = '''    private boolean hasLegacyBookmarks() {
        try {
            Class<?> settingsClass = loadLegacyGuideClass(
                    "io.github.thebusybiscuit.slimefun4.implementation.guide.enhanced.LegacyGuideSettings");
            Object settings = settingsClass.getMethod("get").invoke(null);
            return (boolean) settingsClass.getMethod("hasBookmarks").invoke(settings);
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    private boolean isBookmarked(@Nonnull Player player, @Nonnull SlimefunItem slimefunItem) {
        try {
            Class<?> bookmarksClass = loadLegacyGuideClass(
                    "io.github.thebusybiscuit.slimefun4.implementation.guide.enhanced.LegacyGuideBookmarks");
            Object bookmarks = bookmarksClass.getMethod("get").invoke(null);
            return (boolean) bookmarksClass
                    .getMethod("contains", java.util.UUID.class, String.class)
                    .invoke(bookmarks, player.getUniqueId(), slimefunItem.getId());
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    private void toggleBookmark(@Nonnull Player player, @Nonnull SlimefunItem slimefunItem) {
        final boolean added;
        try {
            Class<?> bookmarksClass = loadLegacyGuideClass(
                    "io.github.thebusybiscuit.slimefun4.implementation.guide.enhanced.LegacyGuideBookmarks");
            Object bookmarks = bookmarksClass.getMethod("get").invoke(null);
            added = (boolean) bookmarksClass
                    .getMethod("toggle", java.util.UUID.class, String.class)
                    .invoke(bookmarks, player.getUniqueId(), slimefunItem.getId());
        } catch (ReflectiveOperationException | LinkageError exception) {
            player.sendMessage(ChatColor.RED + "Bookmarks are unavailable in this Slimefun guide.");
            return;
        }

        player.sendMessage(
                added
                        ? ChatColor.GOLD + "★ Added " + ChatColor.WHITE + ChatColor.stripColor(slimefunItem.getItemName())
                                + ChatColor.GOLD + " to your bookmarks."
                        : ChatColor.YELLOW + "Removed " + ChatColor.WHITE + ChatColor.stripColor(slimefunItem.getItemName())
                                + ChatColor.YELLOW + " from your bookmarks.");
    }

    @Nonnull
    private Class<?> loadLegacyGuideClass(@Nonnull String className) throws ClassNotFoundException {
        return Class.forName(className, true, Slimefun.class.getClassLoader());
    }
'''

if old_toggle in sub:
    sub = sub.replace(old_toggle, new_toggle, 1)
elif 'private boolean hasLegacyBookmarks()' not in sub:
    raise SystemExit('FinalTECH bookmark helper block was not found')

if 'LegacyGuideSettings.get()' in sub or 'LegacyGuideBookmarks.get()' in sub:
    raise SystemExit('Hard Legacy enhanced-guide references remain')

sub_path.write_text(sub, encoding='utf-8')
