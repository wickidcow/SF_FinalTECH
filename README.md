<div align="center">

# FinalTECH — Slimefun Legacy Edition
### Endgame automation, cargo, storage, energy, and progression for Slimefun Legacy

FinalTECH is a large Slimefun addon focused on high-throughput automation, advanced cargo systems, storage, energy handling, powerful tools, and endgame progression.

[![English Audit](https://github.com/wickidcow/SF_FinalTECH/actions/workflows/english-localization-audit.yml/badge.svg)](https://github.com/wickidcow/SF_FinalTECH/actions/workflows/english-localization-audit.yml)
[![License](https://img.shields.io/github/license/wickidcow/SF_FinalTECH?label=license)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![Slimefun](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![Language](https://img.shields.io/badge/Player%20language-English-blue)](#english-first)

[Download](https://github.com/wickidcow/SF_FinalTECH/releases) ·
[Builds](https://github.com/wickidcow/SF_FinalTECH/actions) ·
[Report a Bug](https://github.com/wickidcow/SF_FinalTECH/issues) ·
[Slimefun Legacy](https://github.com/wickidcow/Slimefun-Legacy)

Current maintained version: **3.0**

</div>

> [!IMPORTANT]
> **This is an unofficial, independently maintained English-first continuation of FinalTECH-Changed for the Slimefun Legacy ecosystem.**
> It preserves the Changed edition's gameplay, recipes, item IDs, balance, and saved-data behavior unless a change is specifically documented.
>
> **NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.**

---
## ✨ What is FinalTECH?

FinalTECH expands Slimefun with powerful automation and late-game systems designed for players who want much larger factories and more advanced progression.

| Focus | What it adds |
| --- | --- |
| **Automation** | High-throughput machines, automatic crafting, processing, and production systems |
| **Cargo & logistics** | Point, line, mesh, remote, configurable, and advanced item-transfer systems |
| **Storage** | Large storage units, cards, ports, and specialized storage behavior |
| **Energy** | Generators, capacitors, charging systems, energy transfer, and network tools |
| **Endgame progression** | Entropy, Equivalent Concept, Justifiability, Matrix technology, and other ultimate materials |
| **Utility & tools** | Configuration tools, machine cards, location tools, advanced equipment, and server utilities |
| **English first** | Player-facing names, lore, menus, messages, and maintained documentation are written in English |

This fork uses the **FinalTECH-Changed** line as its gameplay baseline. It does not silently replace Changed mechanics with another FinalTECH port.

---
## 📦 Download and requirements

Release downloads are published as a **direct JAR file**, not a ZIP archive.

Expected release filename:

```text
SF_FinalTECH3.0.jar
```

| Requirement | Current maintained baseline |
| --- | --- |
| **Slimefun** | Slimefun Legacy recommended |
| **Java** | Java 17 or newer |
| **Build API** | Spigot API 1.19.3 |
| **Client** | Normal Minecraft Java client; no client mod required |

Download the latest maintained build from [GitHub Releases](https://github.com/wickidcow/SF_FinalTECH/releases).

> [!WARNING]
> Back up worlds, Slimefun data, plugin data, and player inventories before replacing an existing FinalTECH build. Do not use `/reload` for plugin installation or upgrades.

---
## 🚀 Installation

1. Stop the server normally and create a backup.
2. Install a compatible **Slimefun Legacy** build.
3. Download `SF_FinalTECH3.0.jar` from this repository's [Releases](https://github.com/wickidcow/SF_FinalTECH/releases) page.
4. Place the JAR in the server's `plugins` directory.
5. Remove or archive older FinalTECH JARs so only the intended build can load.
6. Start the server and review the console for dependency or registration errors.
7. Test representative machines, cargo networks, storage, recipes, and endgame items before reopening a production server.

---
## 🌐 English first

English (`en-US`) is the default maintained language for this fork.

The Simplified Chinese (`zh-CN`) and Traditional Chinese (`zh-TW`) locale files are retained as optional reference translations. Source code, default project documentation, maintained configuration text, logs, and player-facing English surfaces are kept English-first.

Established terminology is cross-checked against surviving FinalTECH ports when the same item or mechanic exists. Examples include:

- **Equivalent Concept**
- **Justifiability**
- **Entropy Seed**
- **Entropy Cleaner**
- **Phony**
- **Ordered Dust**
- **Unordered Dust**
- **Matrix Reactor**

The English localization audit rejects unintended CJK text outside the intentionally retained Chinese locale files and also compiles the project as part of CI.

---
## ⚙️ Compatibility and maintenance

FinalTECH-Changed includes substantial balance changes, recipe changes, mechanic adjustments, and bug fixes compared with older FinalTECH builds.

Items from different FinalTECH variants should **not** be assumed to be interchangeable. Never swap variants on a live production world without backups and testing.

This maintained fork currently keeps the Changed edition's existing Java 17 / Maven build baseline while English localization and Slimefun Legacy compatibility are cleaned up in controlled passes. Platform modernization should not silently alter gameplay behavior.

Some startup warnings inherited from the Changed lineage may be harmless when the addon otherwise enables correctly, but unexpected exceptions should still be investigated rather than ignored.

---
## ⚡ Performance notes

FinalTECH is intentionally powerful. Very large cargo networks, automation chains, accelerators, and some endgame mechanics can increase Slimefun tick time.

The Changed lineage contains optimizations compared with much older builds, including fixes for known chain-card TPS problems, but large public servers should still monitor demanding setups and decide whether particularly powerful items need restrictions.

---
## 🌌 Entropy Seed

The **Entropy Seed** (`_FINALTECH_ENTROPY_SEED`) is an endgame progression item used to produce **Equivalent Concept** and **Justifiability**.

When placed, it starts a finite propagation process:

- generated Equivalent Concepts can continue spreading and producing more Equivalent Concept and Justifiability nearby;
- produced Equivalent Concepts eventually convert into Justifiability;
- produced Justifiability automatically disappears after 5 ticks;
- the process is finite, but a full run can create a very large number of temporary objects or material events and may temporarily increase Slimefun tick time.

### Stopping propagation

FinalTECH includes the **Entropy Cleaner**.

Right-click the Entropy Cleaner to enable cleanup mode. While active, it removes Equivalent Concept and Justifiability created by the entropy process. Right-click it again to disable cleanup mode.

On public servers, avoid allowing many Entropy Seeds to be placed at once without testing the performance impact first.

---
## 🧰 Building from source

FinalTECH uses Maven.

```bash
mvn --batch-mode --no-transfer-progress -DskipTests package
```

The shaded production JAR is written to:

```text
target/SF_FinalTECH3.0.jar
```

The repository's release workflow publishes that JAR directly to GitHub Releases so server owners do not have to unpack an Actions artifact ZIP.

---
## 🧬 Lineage and credits

This maintained fork is based on the FinalTECH-Changed lineage, including work by **Final_ROOT**, **QYhB05**, and **balugaq**.

The Changed edition was based on Final_ROOT's FinalTECH build 75 and later continuation work. The original Final_ROOT repository was later deleted or made private, so surviving forks and maintained continuations remain important references for compatibility and terminology.

This repository preserves original authorship and the project's MIT license while maintaining an English-first Slimefun Legacy edition.

---
## 📜 License

FinalTECH-Changed is distributed under the [MIT License](LICENSE). Preserve the license and original credits when redistributing modified builds.
