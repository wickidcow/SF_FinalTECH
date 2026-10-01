package io.taraxacum.libs.plugin.dto;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/** Conservative recovery for known English-locale apostrophe defects, never a general YAML rewrite. */
final class EnglishYamlQuoteRepair {
    private static final Pattern START = Pattern.compile("(?m)^[\\t ]*(?:-[\\t ]+|[A-Za-z0-9_.-]+:[\\t ]+)'");

    private EnglishYamlQuoteRepair() {}

    static void repair(Path file) throws IOException {
        if (!file.getFileName().toString().equalsIgnoreCase("en-US.yml") || !Files.isRegularFile(file)) {
            return;
        }
        String original = Files.readString(file);
        // Bukkit may fold perfectly valid quoted strings across lines when saving.
        // A line-local quote repair must never touch that already-valid document.
        if (valid(original)) {
            return;
        }
        String candidate = repairScalars(original);
        if (candidate.equals(original) || !valid(candidate)) {
            throw new IOException("English locale is invalid and cannot be safely repaired; original file retained: " + file);
        }
        if (!Files.readString(file).equals(original)) {
            throw new IOException("English locale changed during repair; refusing to overwrite it: " + file);
        }
        Path parent = file.toAbsolutePath().getParent();
        Path backup = Files.createTempFile(parent, "en-US.yml.", ".quote-repair.bak");
        Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        Path staged = Files.createTempFile(parent, "en-US.yml.", ".repair.tmp");
        try {
            Files.writeString(staged, candidate);
            try {
                Files.move(staged, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(staged, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    private static boolean valid(String document) {
        try {
            new YamlConfiguration().loadFromString(document);
            return true;
        } catch (InvalidConfigurationException malformed) {
            return false;
        }
    }

    static String repairScalars(String document) {
        var starts = START.matcher(document);
        StringBuilder repaired = new StringBuilder(document.length());
        int copied = 0;
        while (starts.find(copied)) {
            int bodyStart = starts.end();
            int indent = 0;
            while (document.charAt(starts.start() + indent) == ' ' || document.charAt(starts.start() + indent) == '\t') indent++;
            int closing = closingQuote(document, bodyStart, indent);
            if (closing < 0) {
                break; // Unclosed or unfamiliar structure is not permission to invent a value.
            }
            repaired.append(document, copied, bodyStart);
            String body = document.substring(bodyStart, closing);
            for (int index = 0; index < body.length();) {
                if (body.charAt(index) != '\'') {
                    repaired.append(body.charAt(index++));
                    continue;
                }
                int end = index + 1;
                while (end < body.length() && body.charAt(end) == '\'') end++;
                int length = end - index;
                // Retain pairs; heal a lone apostrophe or the known odd triple defect.
                int count = (length & 1) == 0 ? length : length == 1 ? 2 : length - 1;
                repaired.append("'".repeat(count));
                index = end;
            }
            repaired.append('\'');
            copied = closing + 1;
        }
        return repaired.append(document, copied, document.length()).toString();
    }

    private static int closingQuote(String document, int start, int indent) {
        for (int index = start; index < document.length();) {
            if (document.charAt(index) == '\n') {
                int next = index + 1;
                while (next < document.length() && (document.charAt(next) == ' ' || document.charAt(next) == '\t')) next++;
                if (next < document.length() && document.charAt(next) != '\n' && document.charAt(next) != '\r'
                        && next - index - 1 <= indent) return -1;
            }
            if (document.charAt(index) != '\'') {
                index++;
                continue;
            }
            int end = index + 1;
            while (end < document.length() && document.charAt(end) == '\'') end++;
            int tail = end;
            while (tail < document.length() && (document.charAt(tail) == ' ' || document.charAt(tail) == '\t')) tail++;
            boolean endsLine = tail == document.length() || document.charAt(tail) == '\r'
                    || document.charAt(tail) == '\n' || document.charAt(tail) == '#';
            if (((end - index) & 1) != 0 && endsLine) return end - 1;
            index = end;
        }
        return -1;
    }
}
