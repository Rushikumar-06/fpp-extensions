# AGENTS.md - FPP Extensions

These modules own features that used to live in core. Keep command behavior, config defaults, resources, managers, log output, and GUI pages inside the extension that owns the feature.

Common rules:
- Use `FppApi` and addon-facing interfaces/events instead of reaching into core internals.
- Register extension commands with `api.registerCommand(...)`.
- Extension commands should implement `FppAddonCommand.getIcon()`; core help displays addon commands only in the `ADDONS` category.
- Register global or per-bot settings pages with `api.registerSettingsTab(...)` or `api.registerBotSettingsTab(...)`.
- Settings items should return meaningful `getValue()` strings because core displays those values directly in extension GUI categories.
- Put extension defaults in this extension's `src/main/resources/config.yml`; do not copy or depend on the core `config.yml`.
- Keep `settings.gradle.kts` and the root `extensionProjects` list limited to extension directories that exist and build in this workspace. Removed modules must not be carried in `fpp-spoof.jar` manifests or bundle contents.
- Build all first-party extensions with the core Gradle wrapper: from the workspace root run `cmd /c "fake-player-plugin\\gradlew.bat -p fpp-extensions build"`; from this folder run `cmd /c "..\\fake-player-plugin\\gradlew.bat -p . build"`. The build exports individual extension jars and `fpp-spoof.jar` directly to workspace `builds/` (`D:\Coding and AI Projects\FPP Plugin\builds`).
- Do not copy or move extension jars into `fake-player-plugin/build/extensions/`; `builds/` is the final extension output location.
- Do not add extension-specific config to core. For restart-safe per-bot state, use the FPP API's generic extension data methods instead of bespoke storage. Core DB schema v24 also preserves selected first-party extension fields so uninstall/reinstall does not erase saved bot settings.
- Do not bypass normal core spawn/body setup from extensions. Core owns NMS spawn correction, spawn-protection teleport blocking, per-tick fake-player physics, persistence autosaves, Paper/Folia damage fallback, and cross-world damage/knockback state resets. Protection integrations should cancel Bukkit/FPP events or set invulnerability rather than patching core internals.
