# PowahCOE 0.0.6 — Minecraft 1.20.1 / Forge

Backport of the gameplay features in the Minecraft 1.21.1 0.0.6 version.

- Nine input slots, nine output slots and one upgrade slot.
- Upgrade tiers 1–7 target 1–7 stacks of output, constrained by ingredients and output room. No upgrade still means one recipe batch.
- Upgrade level also multiplies energy receive rate. Existing tier and x5 rates remain unchanged; total energy cost scales with batch count.
- Continue crafting while outputs remain, provided compatible output space is available.
- Server-authoritative auto-eject checkbox, synchronized inventories/energy/HUD and full initial chunk state. Missing fields in a network update do not clear saved settings.
- Native Forge GUI with the equivalent slot layout and checkbox; LDLib2 is not required. This is not a pixel-identical port of the 1.21.1 UI.
- Keep JEI catalysts and add optional client-only REI recipe/category/workstation integration.
- Correctly expose the first input to Powah recipe matching.
- Preserve remaining items when auto-eject spans multiple neighboring inventories.
- Migrate legacy 13-slot machine saves to 19 slots, preserving outputs, inputs and upgrade. New saves include an inventory layout version.
- Shift-click upgrade cards into the one-item upgrade slot; updated Chinese/English names and tooltips.
- Preserve the existing 21 crafting recipes and the 20-tick post-craft cooldown.

## Installing

Use `powahcoe-1.20.1-0.0.6.jar` on both client and server, with Minecraft 1.20.1, Forge and the matching Powah installation. Do not use the 1.21.1 JAR. Replace the old PowahCOE JAR rather than loading both. JEI and REI remain optional; this JAR does not bundle them.

Back up worlds before upgrading. Inventory migration is one-way: to downgrade to 0.0.2, restore a pre-upgrade world backup. This does not migrate Minecraft worlds from 1.20.1 to 1.21.1.

## Verification checklist

Verified build: Java 17 `gradlew build` and Forge `reobfJar` passed; all 131 deterministic regression checks passed. All 21 crafting JSON files remain byte-identical to 0.0.2. In-game GUI, multiplayer and optional viewer runtime checks below have not yet been performed.

- `gradlew build` includes deterministic `parityRegression` checks for batch targets, stack sizes, upgrade rate multipliers, overflow and every legacy slot mapping.
- In a disposable world, check all 19 slots; ON/OFF close/reopen; two clients; chunk unload/reload and restart.
- Check first-input-only recipes, batch upgrades 1 and 7, partly occupied/full/wrong-item outputs, and multiple neighboring output inventories.
- Load a copy of an old 0.0.2 world with distinct items in all 13 old slots; verify correct positions and auto-eject, save and reload to confirm migration is not applied twice.
- Check JEI only, REI only, neither viewer, and a dedicated server.

Build/static checks alone do not constitute in-game verification.
