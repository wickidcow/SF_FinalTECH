# FinalTECH-Changed — English Maintained Fork

FinalTECH-Changed is a heavily modified continuation of FinalTECH, focused on high-throughput automation, cargo systems, endgame progression, balance changes, and bug fixes.

This repository is the English-first maintained fork at `wickidcow/SF_FinalTECH`.

## Lineage and credits

The Changed edition is based on Final_ROOT's FinalTECH build 75 and later work by QYhB05 and balugaq. The original Final_ROOT repository was later deleted or made private, so surviving forks and maintained continuations are important references for compatibility and terminology.

This fork keeps the Changed edition as the gameplay baseline. It does **not** silently replace its recipes, balance, mechanics, item IDs, or saved-data behavior with another FinalTECH port.

Established English terminology is cross-checked against `LobbyTech-MC/FinalTech` whenever the same item or mechanic exists there. This avoids conflicting literal translations for established names such as **Equivalent Concept**, **Justifiability**, **Entropy Seed**, **Entropy Cleaner**, **Phony**, **Ordered Dust**, **Unordered Dust**, and **Matrix Reactor**.

## Language support

English (`en-US`) is the default language in this maintained fork.

The Simplified Chinese (`zh-CN`) and Traditional Chinese (`zh-TW`) locale files are retained as optional translations. Source code, documentation, default configuration, logs, and other maintained project text are English-first.

## Compatibility notes

FinalTECH-Changed includes extensive parameter changes, recipe balancing, mechanic adjustments, and bug fixes compared with older FinalTECH builds.

It can coexist with some other FinalTECH variants, but items from different variants should **not** be assumed to be mutually compatible. Do not replace one variant with another on a live world without testing and backups.

Many values are configurable. Machine descriptions and performance expectations generally assume the default settings unless otherwise stated.

Upstream notes that an item-registration warning may appear during startup. In the Changed lineage this warning has been treated as harmless when the plugin otherwise enables normally.

## Performance notes

FinalTECH is powerful by design. Cargo networks, large automation chains, and some endgame mechanics can increase Slimefun tick time. The Changed edition contains optimizations compared with much older builds, including fixes for chain-card TPS problems, but server owners should still monitor demanding setups.

Some machines and items are intentionally powerful enough that administrators may want to restrict them on larger public servers.

## Entropy Seed

The **Entropy Seed** (`_FINALTECH_ENTROPY_SEED`) is an endgame progression item used to produce **Equivalent Concept** and **Justifiability**, two of FinalTECH's ultimate materials.

Under normal progression it is intentionally very expensive and may take a long time to obtain without assistance from other addons or item-copying mechanics.

### What it does

When placed, the Entropy Seed begins a finite propagation process involving Equivalent Concept and Justifiability.

- Generated Equivalent Concepts can continue spreading and producing additional Equivalent Concept and Justifiability nearby.
- Equivalent Concepts produced by the process eventually convert into Justifiability.
- Produced Justifiability automatically disappears after 5 ticks.
- The process is **not infinite**, but one full run can create tens of thousands of temporary objects or material events and may noticeably increase Slimefun tick time for a short period.

### Stopping the propagation

You can either allow the process to finish naturally or use the **Entropy Cleaner** supplied by FinalTECH.

Right-click the Entropy Cleaner to enable cleanup mode. While active, it removes Equivalent Concept and Justifiability created by the entropy process. Right-click it again to disable cleanup mode.

### Server-owner guidance

A single normal use should recover on its own after the propagation finishes. Placing many Entropy Seeds in a short period can produce severe temporary Slimefun tick delay, so public servers should decide how they want to control access.

Possible policies include:

- temporarily enabling the item only when a player is ready to perform the progression step;
- offering a controlled exchange for Equivalent Concept and Justifiability instead of unrestricted seed placement;
- establishing server rules and maintaining good backups;
- allowing normal use while monitoring abuse; or
- disabling the mechanic entirely if its performance profile does not fit the server.

The upstream Changed documentation suggested an example exchange of **1 Phony + 24 stacks of Entropy** for **192 Equivalent Concept + 1024 Justifiability**. Treat that as a historical balance suggestion, not a requirement for this maintained fork.

## Building

The current source still follows the Changed edition's Maven build layout. Further Paper/Slimefun Legacy modernization is tracked separately from this translation pass so localization changes do not alter gameplay behavior.

## License

FinalTECH-Changed is distributed under the MIT License. Preserve the license and original credits when redistributing modified builds.
