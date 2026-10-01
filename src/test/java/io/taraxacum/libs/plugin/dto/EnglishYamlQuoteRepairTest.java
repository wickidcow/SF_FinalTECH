package io.taraxacum.libs.plugin.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EnglishYamlQuoteRepairTest {
    @TempDir Path directory;

    @Test
    void validBukkitResaveIsNotModified() throws Exception {
        String bundled;
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/language/en-US.yml"))) {
            bundled = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        var config = parse(bundled);
        String saved = config.saveToString();
        Path file = write("en-US.yml", saved);
        var entry = ConfigFileManager.class.getDeclaredMethod("repairKnownLanguageSyntax", java.io.File.class);
        entry.setAccessible(true);
        entry.invoke(null, file.toFile());
        assertEquals(saved, Files.readString(file), "A valid Bukkit-saved document must remain byte-identical");
        parse(Files.readString(file));
        assertEquals(1, fileCount());
    }

    @Test
    void preservesValidFoldedApostrophesCommentsAndCrLf() throws Exception {
        String original = "# owner's note\r\nmessage: 'The item''s maximum stack\r\n  size remains unchanged'\r\nvalue: 42\r\n";
        parse(original);
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        assertEquals(original, Files.readString(file));
        assertEquals(1, fileCount());
    }

    @Test
    void repairsKnownTriplesAcrossFoldedLinesAndBacksUpOriginalBytes() throws Exception {
        String original = "message: 'The item'''s maximum stack\n  size remains unchanged'\nvalue: 42\n";
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        var result = parse(Files.readString(file));
        assertEquals("The item's maximum stack size remains unchanged", result.getString("message"));
        assertEquals(42, result.getInt("value"));
        assertBackup(original);
    }

    @Test
    void repairsKnownSingleLineRawApostropheAndPreservesOtherValues() throws Exception {
        String original = "message: 'player's saved item'\nowner: 'unchanged-id'\ncount: 37\n";
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        var result = parse(Files.readString(file));
        assertEquals("player's saved item", result.getString("message"));
        assertEquals("unchanged-id", result.getString("owner"));
        assertEquals(37, result.getInt("count"));
        assertBackup(original);
    }

    @Test
    void preservesCommentQuotesAndValidEscapedApostrophesDuringKnownRepair() throws Exception {
        String original = "message: 'player's saved item' # administrator's note\nother: 'already ''quoted'''\n";
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        String result = Files.readString(file);
        assertTrue(result.contains("# administrator's note"));
        assertEquals("already 'quoted'", parse(result).getString("other"));
        assertBackup(original);
    }

    @Test
    void validDoubleQuotedAndLiteralBlockValuesAreUntouched() throws Exception {
        String original = "message: \"player's literal ''' value\"\nliteral: |\n  - 'don't rewrite this text'\n";
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        assertEquals(original, Files.readString(file));
        assertEquals(1, fileCount());
    }

    @Test
    void repairsListValueWithoutChangingListOrderOrOtherEntries() throws Exception {
        String original = "lines:\n  - 'first'\n  - 'the item's saved\n    value'\n  - 'last'\n";
        Path file = write("en-US.yml", original);
        EnglishYamlQuoteRepair.repair(file);
        assertEquals(List.of("first", "the item's saved value", "last"), parse(Files.readString(file)).getStringList("lines"));
        assertBackup(original);
    }

    @Test
    void unrecognizedStructuralDamageIsNotWrittenOrBackedUpAsASuccess() throws Exception {
        String original = "message: [not closed\n";
        Path file = write("en-US.yml", original);
        assertThrows(IOException.class, () -> EnglishYamlQuoteRepair.repair(file));
        assertEquals(original, Files.readString(file));
        assertEquals(1, fileCount());
    }

    @Test
    void missingQuoteDoesNotAbsorbTheFollowingConfigurationKey() throws Exception {
        String original = "message: 'not closed\nother: 'keep this value'\n";
        Path file = write("en-US.yml", original);
        assertThrows(IOException.class, () -> EnglishYamlQuoteRepair.repair(file));
        assertEquals(original, Files.readString(file));
        assertEquals(1, fileCount());
    }

    @Test
    void repeatedRepairAndBukkitSaveStayStableWithoutExtraBackups() throws Exception {
        Path file = write("en-US.yml", "message: 'item's saved value'\n");
        EnglishYamlQuoteRepair.repair(file);
        for (int index = 0; index < 5; index++) {
            String saved = parse(Files.readString(file)).saveToString();
            Files.writeString(file, saved);
            EnglishYamlQuoteRepair.repair(file);
            assertEquals(saved, Files.readString(file));
        }
        assertEquals(2, fileCount());
    }

    @Test
    void otherFilesAndMissingFilesAreNotModified() throws Exception {
        Path other = write("config.yml", "invalid: [\n");
        EnglishYamlQuoteRepair.repair(other);
        EnglishYamlQuoteRepair.repair(directory.resolve("en-US.yml"));
        assertEquals("invalid: [\n", Files.readString(other));
        assertEquals(1, fileCount());
    }

    @Test
    void configurationEntryPointRefusesToLoadAndOverwriteUnrepairableFile() throws Exception {
        Path file = write("en-US.yml", "invalid: [\n");
        var entry = ConfigFileManager.class.getDeclaredMethod("repairKnownLanguageSyntax", java.io.File.class);
        entry.setAccessible(true);
        var thrown = assertThrows(InvocationTargetException.class, () -> entry.invoke(null, file.toFile()));
        assertInstanceOf(IllegalStateException.class, thrown.getCause());
        assertEquals("invalid: [\n", Files.readString(file));
        assertEquals(1, fileCount());
    }

    private Path write(String name, String content) throws IOException {
        Path file = directory.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    private static YamlConfiguration parse(String text) throws Exception {
        var config = new YamlConfiguration();
        config.loadFromString(text);
        return config;
    }

    private long fileCount() throws IOException {
        try (var files = Files.list(directory)) { return files.count(); }
    }

    private void assertBackup(String expected) throws IOException {
        try (var files = Files.list(directory)) {
            var backups = files.filter(p -> p.toString().endsWith(".quote-repair.bak")).toList();
            assertEquals(1, backups.size());
            assertEquals(expected, Files.readString(backups.getFirst()));
        }
        assertEquals(2, fileCount(), "No temporary file should remain after repair");
    }
}
