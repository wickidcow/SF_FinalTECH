package io.taraxacum.libs.plugin.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Keeps existing immutable components intact while honoring the public String-based edit contract. */
final class ComponentLore {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private ComponentLore() {
    }

    static List<Component> prepend(@Nullable List<Component> existing, String line) {
        List<Component> result = copy(existing);
        result.add(0, LEGACY.deserialize(line));
        return result;
    }

    static List<Component> append(@Nullable List<Component> existing, String... lines) {
        List<Component> result = copy(existing);
        for (String line : lines) {
            result.add(LEGACY.deserialize(line));
        }
        return result;
    }

    static List<Component> setLast(@Nullable List<Component> existing, String line) {
        List<Component> result = copy(existing);
        Component replacement = LEGACY.deserialize(line);
        if (result.isEmpty()) {
            result.add(replacement);
        } else {
            result.set(result.size() - 1, replacement);
        }
        return result;
    }

    @Nullable
    static List<Component> removeLast(@Nullable List<Component> existing) {
        if (existing == null) {
            return null;
        }
        return new ArrayList<>(existing.subList(0, Math.max(existing.size() - 1, 0)));
    }

    @Nullable
    static String last(@Nullable List<Component> existing) {
        return existing == null || existing.isEmpty() ? null : LEGACY.serialize(existing.get(existing.size() - 1));
    }

    static List<Component> replace(@Nullable List<Component> existing, int offset, List<String> lines) {
        List<Component> result = offset < 0 ? new ArrayList<>() : copy(existing);
        int start = Math.max(offset, 0);
        while (result.size() < start) {
            result.add(Component.empty());
        }
        for (int i = 0; i < lines.size(); i++) {
            Component replacement = LEGACY.deserialize(lines.get(i));
            if (result.size() <= start + i) {
                result.add(replacement);
            } else {
                result.set(start + i, replacement);
            }
        }
        return result;
    }

    private static List<Component> copy(@Nullable List<Component> existing) {
        return existing == null ? new ArrayList<>(8) : new ArrayList<>(existing);
    }
}
