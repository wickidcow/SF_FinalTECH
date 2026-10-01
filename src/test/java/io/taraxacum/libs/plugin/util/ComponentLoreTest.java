package io.taraxacum.libs.plugin.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

/** Tests the exact component helper used by ItemStackUtil, without substituting a server or item codec. */
class ComponentLoreTest {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Test
    void prependRetainsTheOriginalRichComponents() {
        List<Component> original = List.of(rich("one"), rich("two"));
        List<Component> updated = ComponentLore.prepend(original, "§bHeader");
        assertEquals(LEGACY.deserialize("§bHeader"), updated.get(0));
        assertSame(original.get(0), updated.get(1));
        assertSame(original.get(1), updated.get(2));
        assertEquals(2, original.size());
    }

    @Test
    void appendPreservesRichPrefixAndReturnsADetachedList() {
        List<Component> original = List.of(rich("one"), rich("two"));
        List<Component> updated = ComponentLore.append(original, "§6Status", "§f42");
        assertSame(original.get(0), updated.get(0));
        assertSame(original.get(1), updated.get(1));
        assertEquals(LEGACY.deserialize("§6Status"), updated.get(2));
        assertEquals(LEGACY.deserialize("§f42"), updated.get(3));
        updated.clear();
        assertEquals(2, original.size());
    }

    @Test
    void setLastReplacesOnlyTheLastLine() {
        List<Component> original = List.of(rich("retained"), rich("replaced"));
        List<Component> updated = ComponentLore.setLast(original, "§7Tail");
        assertSame(original.get(0), updated.get(0));
        assertEquals(LEGACY.deserialize("§7Tail"), updated.get(1));
        assertEquals(rich("replaced"), original.get(1));
    }

    @Test
    void removalDoesNotFlattenTheRemainingLore() {
        List<Component> original = List.of(rich("retained"), rich("removed"));
        List<Component> updated = ComponentLore.removeLast(original);
        assertEquals(1, updated.size());
        assertSame(original.get(0), updated.get(0));
        assertEquals(2, original.size());
        assertTrue(ComponentLore.removeLast(List.of(rich("single"))).isEmpty());
    }

    @Test
    void lastReturnsTheHistoricalStringWithoutModifyingOtherLines() {
        Component tail = Component.text("Value", NamedTextColor.AQUA).decorate(TextDecoration.BOLD);
        List<Component> original = List.of(rich("retained"), tail);
        assertEquals(LEGACY.serialize(tail), ComponentLore.last(original));
        assertEquals(rich("retained"), original.get(0));
        assertSame(tail, original.get(1));
        assertNull(ComponentLore.last(null));
        assertNull(ComponentLore.last(List.of()));
    }

    @Test
    void absentAndEmptyLoreKeepHistoricalCreationRules() {
        for (List<Component> original : Arrays.<List<Component>>asList(null, List.of())) {
            assertEquals(List.of(LEGACY.deserialize("§cfirst")), ComponentLore.prepend(original, "§cfirst"));
            assertEquals(List.of(LEGACY.deserialize("§clast")), ComponentLore.setLast(original, "§clast"));
            assertEquals(List.of(LEGACY.deserialize("§ca"), Component.empty()), ComponentLore.append(original, "§ca", ""));
            assertTrue(ComponentLore.append(original).isEmpty());
            assertEquals(List.of(Component.empty(), Component.empty(), LEGACY.deserialize("§ctail")),
                    ComponentLore.replace(original, 2, List.of("§ctail")));
        }
        assertNull(ComponentLore.removeLast(null));
        assertTrue(ComponentLore.removeLast(List.of()).isEmpty());
    }

    @Test
    void replacementKeepsRichPrefixAndSuffixAndPadsOnlyMissingPositions() {
        List<Component> original = List.of(rich("prefix"), rich("replace"), rich("suffix"));
        List<Component> updated = ComponentLore.replace(original, 1, List.of("§aNew"));
        assertSame(original.get(0), updated.get(0));
        assertEquals(LEGACY.deserialize("§aNew"), updated.get(1));
        assertSame(original.get(2), updated.get(2));
        List<Component> padded = ComponentLore.replace(original, 5, List.of("§bEnd"));
        assertEquals(6, padded.size());
        assertSame(original.get(2), padded.get(2));
        assertEquals(Component.empty(), padded.get(3));
        assertEquals(Component.empty(), padded.get(4));
        assertEquals(LEGACY.deserialize("§bEnd"), padded.get(5));
    }

    @Test
    void negativeOffsetReplacesEverythingInsteadOfRetainingThePrefix() {
        List<Component> original = List.of(rich("old"), rich("other"));
        assertEquals(List.of(LEGACY.deserialize("§aNew")), ComponentLore.replace(original, -1, List.of("§aNew")));
        assertTrue(ComponentLore.replace(original, Integer.MIN_VALUE, List.of()).isEmpty());
        assertEquals(2, original.size());
    }

    @Test
    void emptyReplacementStillPerformsHistoricalPadding() {
        List<Component> original = List.of(rich("old"));
        List<Component> updated = ComponentLore.replace(original, 3, List.of());
        assertEquals(3, updated.size());
        assertSame(original.get(0), updated.get(0));
        assertEquals(List.of(Component.empty(), Component.empty()), updated.subList(1, 3));
        List<Component> unchanged = ComponentLore.replace(original, 0, List.of());
        assertNotSame(original, unchanged);
        assertSame(original.get(0), unchanged.get(0));
    }

    @Test
    void generatedStringsUseTheExistingLegacySerializerExactly() {
        List<String> generated = List.of("", "§8[Status] §aEnabled", "§lBold§r plain", "§bOwner: §fNone",
                "Unicode: Ω ⌫", "§x§1§2§3§4§5§6Hex");
        List<Component> updated = ComponentLore.append(null, generated.toArray(String[]::new));
        for (int i = 0; i < generated.size(); i++) {
            assertEquals(LEGACY.deserialize(generated.get(i)), updated.get(i));
        }
    }

    @Test
    void invalidGeneratedLinesDoNotMutateInputCollections() {
        List<Component> original = new ArrayList<>(List.of(rich("old")));
        List<Component> before = List.copyOf(original);
        assertThrows(RuntimeException.class, () -> ComponentLore.append(original, "accepted", null));
        assertThrows(RuntimeException.class, () -> ComponentLore.prepend(original, null));
        assertThrows(RuntimeException.class, () -> ComponentLore.setLast(original, null));
        assertThrows(RuntimeException.class, () -> ComponentLore.replace(original, 0, Arrays.asList("accepted", null)));
        assertEquals(before, original);
    }

    @Test
    void repeatedAppendRetainsTheExistingNonDeduplicatingContract() {
        Component prefix = rich("prefix");
        List<Component> result = List.of(prefix);
        for (int i = 0; i < 20; i++) result = ComponentLore.append(result, "§bRepeated");
        assertEquals(21, result.size());
        assertSame(prefix, result.get(0));
        for (int i = 1; i < result.size(); i++) assertEquals(LEGACY.deserialize("§bRepeated"), result.get(i));
    }

    @Test
    void nativeResultsMatchTheFormerAlgorithmAcross4500SeededComparisons() {
        Random random = new Random(371);
        String[] palette = {"", "plain", "§aGreen", "§bOwner: §fExisting", "§lBold§r plain", "§7Ω data"};
        int comparisons = 0;
        for (int layout = 0; layout < 500; layout++) {
            List<Component> original = random.nextInt(5) == 0 ? null : new ArrayList<>();
            if (original != null) {
                int size = random.nextInt(12);
                for (int i = 0; i < size; i++) original.add(LEGACY.deserialize(palette[random.nextInt(palette.length)]));
            }
            List<Component> copy = original == null ? null : List.copyOf(original);
            String line = palette[random.nextInt(palette.length)];
            List<String> generated = List.of(line, "§7Second", "");
            int offset = random.nextInt(18);
            for (int operation = 0; operation < 8; operation++) {
                List<Component> expected = former(original, operation, offset, line, generated);
                List<Component> actual = switch (operation) {
                    case 0 -> ComponentLore.prepend(original, line);
                    case 1 -> ComponentLore.append(original, line);
                    case 2 -> ComponentLore.append(original, generated.toArray(String[]::new));
                    case 3 -> ComponentLore.setLast(original, line);
                    case 4 -> ComponentLore.removeLast(original);
                    case 5 -> ComponentLore.replace(original, offset, generated);
                    case 6 -> ComponentLore.replace(original, offset, List.of());
                    case 7 -> ComponentLore.replace(original, -1, generated);
                    default -> throw new AssertionError(operation);
                };
                assertEquals(serialized(expected), serialized(actual), "Layout " + layout + ", operation " + operation);
                assertEquals(copy, original, "Input must remain unchanged");
                comparisons++;
            }
            List<String> oldStrings = serialized(original);
            String expectedLast = oldStrings == null || oldStrings.isEmpty() ? null : oldStrings.get(oldStrings.size() - 1);
            assertEquals(expectedLast, ComponentLore.last(original));
            comparisons++;
        }
        assertEquals(4500, comparisons);
    }

    @Test
    void allThirteenEditedPublicDescriptorsRemainAvailable() throws ReflectiveOperationException {
        // Class lookup deliberately does not initialize server-dependent static fields.
        Class<?> type = Class.forName("io.taraxacum.libs.plugin.util.ItemStackUtil", false, getClass().getClassLoader());
        require(type, "addLoreToFirst", void.class, ItemStack.class, String.class);
        for (Class<?> target : List.of(ItemStack.class, ItemMeta.class)) {
            require(type, "addLoreToLast", void.class, target, String.class);
            require(type, "addLoresToLast", void.class, target, String[].class);
            require(type, "removeLastLore", void.class, target);
            require(type, "setLastLore", void.class, target, String.class);
            require(type, "getLastLore", String.class, target);
        }
        require(type, "replaceLore", void.class, ItemStack.class, int.class, String[].class);
        require(type, "replaceLore", void.class, ItemStack.class, int.class, List.class);
    }

    private static void require(Class<?> type, String name, Class<?> result, Class<?>... parameters)
            throws NoSuchMethodException {
        var method = type.getDeclaredMethod(name, parameters);
        assertEquals(result, method.getReturnType());
        assertTrue(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()));
    }

    private static Component rich(String suffix) {
        return Component.translatable("item.minecraft.diamond").append(Component.text(suffix))
                .color(NamedTextColor.LIGHT_PURPLE).font(Key.key("existing:font"))
                .hoverEvent(HoverEvent.showText(Component.text("Existing hover")))
                .insertion("existing-insertion");
    }

    private static List<String> serialized(List<Component> input) {
        if (input == null) return null;
        List<String> result = new ArrayList<>();
        for (Component value : input) result.add(LEGACY.serialize(value));
        return result;
    }

    /** Literal prior String-list decisions, followed by the old complete-list deserialization. */
    private static List<Component> former(List<Component> original, int operation, int offset,
            String line, List<String> generated) {
        List<String> oldLore = serialized(original);
        if (operation == 4 && oldLore == null) return null;
        List<String> lore = oldLore == null ? new ArrayList<>() : new ArrayList<>(oldLore);
        switch (operation) {
            case 0 -> lore.add(0, line);
            case 1 -> lore.add(line);
            case 2 -> lore.addAll(generated);
            case 3 -> {
                if (lore.isEmpty()) lore.add(line);
                else lore.set(lore.size() - 1, line);
            }
            case 4 -> {
                if (!lore.isEmpty()) lore = lore.subList(0, lore.size() - 1);
            }
            case 5, 6 -> {
                while (lore.size() < offset) lore.add("");
                List<String> replacement = operation == 6 ? List.of() : generated;
                for (int i = 0; i < replacement.size(); i++) {
                    if (lore.size() <= offset + i) lore.add(offset + i, replacement.get(i));
                    else lore.set(offset + i, replacement.get(i));
                }
            }
            case 7 -> lore = new ArrayList<>(generated);
            default -> throw new AssertionError(operation);
        }
        List<Component> result = new ArrayList<>();
        for (String value : lore) result.add(LEGACY.deserialize(value));
        return result;
    }
}
