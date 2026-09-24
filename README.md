# Powah Cable Orb Extension — Minecraft 1.20.1

Forge / Java 17 source for **0.0.6**. The [main branch](https://github.com/960545213/powahcoe/tree/main) targets Minecraft 1.21.1 / NeoForge; do not mix their JARs.

## Features

- Seven tiers of cable orbs, with normal and x5 variants.
- Nine input slots, nine output slots and one upgrade slot.
- Seven upgrade levels target 1–7 output stacks and scale energy receive rate.
- Continuous crafting while compatible output space remains.
- Server-authoritative auto-eject checkbox and synchronized inventory/energy state.
- Legacy 13-slot inventory migration preserving items and auto-eject state.
- Optional JEI and REI integration, with a native Forge GUI (no LDLib2 dependency).

See [the changelog](CHANGELOG-1.20.1-0.0.6.md) for details. Install the matching Powah mod and PowahCOE on both client and server. Back up worlds before upgrading; reverting the inventory layout requires a pre-upgrade backup.

## Build

Install JDK 17. Download the three pinned development dependencies using PowerShell 7:

```powershell
pwsh -File scripts/fetch-dependencies.ps1
./gradlew.bat --no-daemon build
```

On Linux/macOS, run the same dependency script with PowerShell 7 and then `./gradlew --no-daemon build`. The script downloads official Modrinth-hosted files and checks SHA-256; dependencies are not committed or bundled in the output. Existing files must match the pinned hashes.

Output: `build/libs/powahcoe-1.20.1-0.0.6.jar`.

`build` includes reobfuscation and `parityRegression` (131 deterministic assertions covering batching, limits and real NBT inventory migration). Run `./gradlew parityRegression` for these checks alone. In-game GUI, multiplayer and optional REI runtime testing has not yet been completed.

The CI workflow uses Java 17, fetches the same verified dependencies and uploads the built JAR as a workflow artifact. For development game launches, install Powah and its required dependencies in the development environment; the current build declares Powah compile-only.

