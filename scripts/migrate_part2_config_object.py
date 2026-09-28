from pathlib import Path
import re
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
JAVA = ROOT / "src/main/java"
CONFIG_IMPORT = "import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;\n"
TICKER_IMPORT = "import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;\n"


def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")


def write(rel, source):
    (ROOT / rel).write_text(source, encoding="utf-8")


def replace_once(source, old, new, label):
    count = source.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected exactly one target, found {count}")
    return source.replace(old, new, 1)


def add_import(source, imp):
    if imp in source:
        return source
    lines = source.splitlines(keepends=True)
    import_indexes = [i for i, line in enumerate(lines) if line.startswith("import ")]
    if not import_indexes:
        raise RuntimeError("Cannot insert import: no import block")
    lines.insert(max(import_indexes) + 1, imp)
    return "".join(lines)


machine_root = JAVA / "io/taraxacum/finaltech/core/item/machine"

# Internal machine tick data becomes opaque Object. The RC-37 Config type remains
# only at explicit compatibility boundaries.
for path in machine_root.rglob("*.java"):
    rel = path.relative_to(ROOT).as_posix()
    source = path.read_text(encoding="utf-8")

    if path.name != "AbstractMachine.java":
        if rel not in {
            "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractEnergyProviderMachine.java",
            "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java",
        }:
            source = source.replace("@Nonnull Config config", "@Nonnull Object config")
            source = source.replace("@Nonnull Config ignored", "@Nonnull Object ignored")
            source = source.replace("@Nonnull Config data", "@Nonnull Object data")
    else:
        source = replace_once(
            source,
            "    protected abstract void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config);",
            "    protected abstract void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Object data);",
            "AbstractMachine internal tick contract",
        )

    had_direct = bool(re.search(r"\bconfig\.(?:contains|getString|setValue)\(", source))
    source = re.sub(
        r"\bconfig\.contains\(([^\n;]+?)\)",
        r"LegacyTickerDataCompat.contains(config, \1)",
        source,
    )
    source = re.sub(
        r"\bconfig\.getString\(([^\n;]+?)\)",
        r"LegacyTickerDataCompat.getString(config, \1)",
        source,
    )
    source = re.sub(
        r"\bconfig\.setValue\(([^,\n]+),\s*([^\n;]+?)\)",
        r"LegacyTickerDataCompat.setValue(config, \1, \2)",
        source,
    )
    if had_direct:
        source = add_import(source, TICKER_IMPORT)

    # Do not retain broad suppressions after the deprecated tick type is gone.
    if rel != "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractMachine.java":
        source = re.sub(
            r'(?m)^(\s*)@SuppressWarnings\("deprecation"\)\n(?=\s*(?:protected|public)\s+(?:final\s+)?void\s+tick\s*\()',
            "",
            source,
        )

    keep_config = rel in {
        "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractMachine.java",
        "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractEnergyProviderMachine.java",
        "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java",
    }
    if not keep_config:
        source = source.replace(CONFIG_IMPORT, "")

    path.write_text(source, encoding="utf-8")

# These two bases no longer expose the deprecated type themselves.
rel = "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractConfigFreeMachine.java"
source = read(rel)
source = source.replace(
    """ * <p>The deprecated RC-37 Config signature is isolated here so data-free machine
 * implementations can remain independent of the legacy storage ABI. Current
 * Slimefun Legacy still reaches this bridge through BlockTicker's compatibility
 * dispatch, preserving identical tick timing and behavior.</p>
 */
@SuppressWarnings("deprecation")""",
    """ * <p>The RC-37 Config signature is isolated in {@link AbstractMachine}; this
 * subclass receives opaque ticker data and intentionally ignores it.</p>
 */""",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractTickerDataMachine.java"
source = read(rel)
source = source.replace(
    """ * <p>The same Config instance supplied by Slimefun is forwarded as Object to
 * avoid allocating wrappers in machine hot paths. Access is centralized through
 * LegacyTickerDataCompat, keeping the deprecated type out of machine code while
 * preserving exact RC-37 behavior.</p>
 */
@SuppressWarnings("deprecation")""",
    """ * <p>The storage object supplied by Slimefun is forwarded as {@link Object} to
 * avoid allocating wrappers in machine hot paths. Access is centralized through
 * compatibility helpers, keeping deprecated storage types out of machine code
 * while preserving exact RC-37 behavior.</p>
 */""",
)
write(rel, source)

# Expanded capacitor keeps its public Config overload for source/binary compatibility,
# but all maintained internals use Object.
rel = "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java"
source = read(rel)
source = source.replace(
    "@Nonnull Config config) {\n        String energyStr",
    "@Nonnull Object config) {\n        String energyStr",
    1,
)
source = replace_once(
    source,
    """    @SuppressWarnings("deprecation")
    public int getStack(@Nonnull Config config) {
        return Integer.parseInt(JavaUtil.getFirstNotNull(LegacyTickerDataCompat.getString(config, this.key), StringNumberUtil.ZERO));
    }""",
    """    public int getStack(@Nonnull Object data) {
        return Integer.parseInt(JavaUtil.getFirstNotNull(LegacyTickerDataCompat.getString(data, this.key), StringNumberUtil.ZERO));
    }

    /**
     * RC-37 source/binary compatibility overload.
     */
    @SuppressWarnings("deprecation")
    public int getStack(@Nonnull Config config) {
        return getStack((Object) config);
    }""",
    "expanded capacitor getStack",
)
write(rel, source)

# Shared storage helpers expose Object-first implementations while retaining Config wrappers.
rel = "src/main/java/io/taraxacum/libs/slimefun/dto/BlockStorageHelper.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @Nonnull
    public String getOrDefaultValue(@Nonnull Config config) {
        return config.contains(this.getKey()) ? config.getString(this.getKey()) : this.defaultValue();
    }""",
    """    @Nonnull
    public String getOrDefaultValue(@Nonnull Object data) {
        return LegacyTickerDataCompat.contains(data, this.getKey())
                ? LegacyTickerDataCompat.getString(data, this.getKey())
                : this.defaultValue();
    }

    /**
     * RC-37 compatibility overload. New FinalTECH ticker code passes opaque data.
     */
    @Nonnull
    public String getOrDefaultValue(@Nonnull Config config) {
        return this.getOrDefaultValue((Object) config);
    }""",
    "BlockStorageHelper getOrDefaultValue",
)
source = replace_once(
    source,
    """    public void setOrClearValue(@Nonnull Config config, @Nullable String value) {
        config.setValue(this.getKey(), value);
    }""",
    """    public void setOrClearValue(@Nonnull Object data, @Nullable String value) {
        LegacyTickerDataCompat.setValue(data, this.getKey(), value);
    }

    /**
     * RC-37 compatibility overload. New FinalTECH ticker code passes opaque data.
     */
    public void setOrClearValue(@Nonnull Config config, @Nullable String value) {
        this.setOrClearValue((Object) config, value);
    }""",
    "BlockStorageHelper setOrClearValue",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @Nonnull
    @SuppressWarnings("deprecation")
    public static BlockFace[] getBlockFaces(@Nonnull Config config, @Nonnull String... values) {
        String string = JavaUtil.getFirstNotNull(config.getString(KEY), "");
        return PositionInfo.getBlockFaces(string, values);
    }""",
    """    @Nonnull
    public static BlockFace[] getBlockFaces(@Nonnull Object data, @Nonnull String... values) {
        String string = JavaUtil.getFirstNotNull(LegacyTickerDataCompat.getString(data, KEY), "");
        return PositionInfo.getBlockFaces(string, values);
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public static BlockFace[] getBlockFaces(@Nonnull Config config, @Nonnull String... values) {
        return PositionInfo.getBlockFaces((Object) config, values);
    }""",
    "PositionInfo getBlockFaces",
)
source = replace_once(
    source,
    """        @Nonnull
        @Override
        @SuppressWarnings("deprecation")
        public String getOrDefaultValue(@Nonnull Config config) {
            String valueMap = config.getString(this.getKey());""",
    """        @Nonnull
        @Override
        public String getOrDefaultValue(@Nonnull Object data) {
            String valueMap = LegacyTickerDataCompat.getString(data, this.getKey());""",
    "PositionInfo helper override",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/util/PermissionUtil.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @SuppressWarnings("deprecation")
    public static boolean checkOfflinePermission(@Nonnull Location sourceLocation, @Nonnull Config config, @Nonnull Location... targetLocations) {
        return PermissionUtil.checkOfflinePermission(
                sourceLocation,
                config.getString(ConstantTableUtil.CONFIG_UUID),
                IgnorePermission.VALUE_TRUE.equals(IgnorePermission.HELPER.getOrDefaultValue(config)),
                targetLocations);
    }""",
    """    public static boolean checkOfflinePermission(@Nonnull Location sourceLocation, @Nonnull Object data, @Nonnull Location... targetLocations) {
        return PermissionUtil.checkOfflinePermission(
                sourceLocation,
                LegacyTickerDataCompat.getString(data, ConstantTableUtil.CONFIG_UUID),
                IgnorePermission.VALUE_TRUE.equals(IgnorePermission.HELPER.getOrDefaultValue(data)),
                targetLocations);
    }

    @SuppressWarnings("deprecation")
    public static boolean checkOfflinePermission(@Nonnull Location sourceLocation, @Nonnull Config config, @Nonnull Location... targetLocations) {
        return PermissionUtil.checkOfflinePermission(sourceLocation, (Object) config, targetLocations);
    }""",
    "PermissionUtil object bridge",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/util/AntiAccelerationUtil.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @SuppressWarnings("deprecation")
    public static boolean isAccelerated(@Nonnull Config config) {
        String s = config.getString(KEY);
        if (s != null && Integer.parseInt(s) == FinalTechChanged.getSlimefunTickCount()) {
            return true;
        }
        config.setValue(KEY, String.valueOf(FinalTechChanged.getSlimefunTickCount()));
        return false;
    }""",
    """    public static boolean isAccelerated(@Nonnull Object data) {
        String value = LegacyTickerDataCompat.getString(data, KEY);
        if (value != null && Integer.parseInt(value) == FinalTechChanged.getSlimefunTickCount()) {
            return true;
        }
        LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(FinalTechChanged.getSlimefunTickCount()));
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean isAccelerated(@Nonnull Config config) {
        return isAccelerated((Object) config);
    }""",
    "AntiAccelerationUtil object bridge",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/util/PerformanceLimitUtil.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @SuppressWarnings("deprecation")
    public static boolean charge(@Nonnull Config config) {
        int charge = config.contains(KEY) ? Integer.parseInt(config.getString(KEY)) : 0;
        charge += FinalTechChanged.getTps();
        if (charge >= 20) {
            if (charge >= 40) {
                charge -= 20;
            }
            config.setValue(KEY, String.valueOf(charge - 20));
            return true;
        } else {
            config.setValue(KEY, String.valueOf(charge));
            return false;
        }
    }""",
    """    public static boolean charge(@Nonnull Object data) {
        int charge = LegacyTickerDataCompat.contains(data, KEY)
                ? Integer.parseInt(LegacyTickerDataCompat.getString(data, KEY))
                : 0;
        charge += FinalTechChanged.getTps();
        if (charge >= 20) {
            if (charge >= 40) {
                charge -= 20;
            }
            LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(charge - 20));
            return true;
        } else {
            LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(charge));
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean charge(@Nonnull Config config) {
        return charge((Object) config);
    }""",
    "PerformanceLimitUtil object bridge",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/libs/slimefun/util/EnergyUtil.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    @Nonnull
    public static String getCharge(@Nonnull Config config) {
        return Objects.requireNonNull(JavaUtil.getFirstNotNull(config.getString(ConstantTableUtil.CONFIG_CHARGE), StringNumberUtil.ZERO));
    }""",
    """    @Nonnull
    public static String getCharge(@Nonnull Object data) {
        return Objects.requireNonNull(JavaUtil.getFirstNotNull(
                LegacyTickerDataCompat.getString(data, ConstantTableUtil.CONFIG_CHARGE),
                StringNumberUtil.ZERO));
    }

    @Nonnull
    public static String getCharge(@Nonnull Config config) {
        return getCharge((Object) config);
    }""",
    "EnergyUtil getCharge",
)
source = replace_once(
    source,
    """    public static void setCharge(@Nonnull Config config, @Nonnull String energy) {
        config.setValue(ConstantTableUtil.CONFIG_CHARGE, energy);
    }

    public static void setCharge(@Nonnull Config config, int energy) {
        config.setValue(ConstantTableUtil.CONFIG_CHARGE, String.valueOf(energy));
    }""",
    """    public static void setCharge(@Nonnull Object data, @Nonnull String energy) {
        LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_CHARGE, energy);
    }

    public static void setCharge(@Nonnull Object data, int energy) {
        LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_CHARGE, String.valueOf(energy));
    }

    public static void setCharge(@Nonnull Config config, @Nonnull String energy) {
        setCharge((Object) config, energy);
    }

    public static void setCharge(@Nonnull Config config, int energy) {
        setCharge((Object) config, energy);
    }""",
    "EnergyUtil setCharge",
)
write(rel, source)

rel = "src/main/java/io/taraxacum/finaltech/util/BlockTickerUtil.java"
source = add_import(read(rel), TICKER_IMPORT)
source = replace_once(
    source,
    """    public static void setSleep(@Nonnull Config config, @Nullable String sleep) {
        config.setValue(ConstantTableUtil.CONFIG_SLEEP, sleep);
    }

    public static boolean hasSleep(@Nonnull Config config) {
        return config.contains(ConstantTableUtil.CONFIG_SLEEP);
    }

    public static void subSleep(@Nonnull Config config) {
        String sleepStr = config.getString(ConstantTableUtil.CONFIG_SLEEP);
        if (sleepStr != null) {
            double sleep = Double.parseDouble(sleepStr) - 1;
            if (sleep > 0) {
                config.setValue(ConstantTableUtil.CONFIG_SLEEP, String.valueOf(sleep));
            } else {
                config.setValue(ConstantTableUtil.CONFIG_SLEEP, "0");
            }
        }
    }""",
    """    public static void setSleep(@Nonnull Object data, @Nullable String sleep) {
        LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_SLEEP, sleep);
    }

    public static boolean hasSleep(@Nonnull Object data) {
        return LegacyTickerDataCompat.contains(data, ConstantTableUtil.CONFIG_SLEEP);
    }

    public static void subSleep(@Nonnull Object data) {
        String sleepStr = LegacyTickerDataCompat.getString(data, ConstantTableUtil.CONFIG_SLEEP);
        if (sleepStr != null) {
            double sleep = Double.parseDouble(sleepStr) - 1;
            if (sleep > 0) {
                LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_SLEEP, String.valueOf(sleep));
            } else {
                LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_SLEEP, "0");
            }
        }
    }

    public static void setSleep(@Nonnull Config config, @Nullable String sleep) {
        setSleep((Object) config, sleep);
    }

    public static boolean hasSleep(@Nonnull Config config) {
        return hasSleep((Object) config);
    }

    public static void subSleep(@Nonnull Config config) {
        subSleep((Object) config);
    }""",
    "BlockTickerUtil sleep helpers",
)
write(rel, source)

remaining_direct = []
for path in machine_root.rglob("*.java"):
    source = path.read_text(encoding="utf-8")
    if re.search(r"\bconfig\.(?:contains|getString|setValue)\(", source):
        remaining_direct.append(path)
if remaining_direct:
    raise RuntimeError(
        "direct machine Config access remains: "
        + ", ".join(str(path.relative_to(ROOT)) for path in remaining_direct)
    )

allowed = {
    machine_root / "AbstractMachine.java",
    machine_root / "AbstractEnergyProviderMachine.java",
    machine_root / "electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java",
}
imports = [
    path
    for path in machine_root.rglob("*.java")
    if CONFIG_IMPORT in path.read_text(encoding="utf-8")
]
if set(imports) != allowed:
    raise RuntimeError(
        "unexpected machine Config imports: "
        + str([str(path.relative_to(ROOT)) for path in imports])
    )

print("Part 2 Config object migration prepared successfully.")
