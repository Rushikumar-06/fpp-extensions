# AGENTS.md - fpp-swap

Owns bot swap behavior.

Keep here:
- `FppSwapExtension`
- `BotSwapAI`
- `/fpp swap`
- swap GUI
- swap/rejoin scheduling and retry config

Core should not start swap timers, print swap feature status, or register swap GUI items. Use addon commands/settings to expose controls.

## Current Integration Notes
- Addon commands should implement `FppAddonCommand.getIcon()` and are shown only in the core help GUI `ADDONS` category.
- Extension settings items should return useful `getValue()` text; core displays that value directly in extension GUI categories.
- Build through `../fpp-extensions` with `cmd /c "..\\fake-player-plugin\\gradlew.bat -p . build"` from `fpp-extensions/`, or `cmd /c "fake-player-plugin\\gradlew.bat -p fpp-extensions build"` from the workspace root. This module is included in `fpp-spoof.jar` and final jars are written directly to workspace `builds/`.
- Keep config in this extension's data folder. Use extension-owned storage or FPP's generic extension data API for per-bot state; core DB schema v24 preserves selected first-party extension fields for uninstall/reinstall safety.
- Do not bypass normal core fake-player body setup. Core owns NMS spawn correction, spawn-protection teleport blocking, per-tick physics, persistence autosaves, Paper/Folia damage fallback, and cross-world damage/knockback state resets.
