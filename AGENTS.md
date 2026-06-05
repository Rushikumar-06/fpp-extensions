# AGENTS.md - FPP Extensions

These modules own features that used to live in core. Keep command behavior, config defaults, resources, managers, log output, and GUI pages inside the extension that owns the feature.

Common rules:
- Use `FppApi` and addon-facing interfaces/events instead of reaching into core internals.
- Register extension commands with `api.registerCommand(...)`.
- Extension commands should implement `FppAddonCommand.getIcon()`; core help displays addon commands only in the `ADDONS` category.
- Register global or per-bot settings pages with `api.registerSettingsTab(...)` or `api.registerBotSettingsTab(...)`.
- Settings items should return meaningful `getValue()` strings because core displays those values directly in extension GUI categories.
- Put extension defaults in this extension's `src/main/resources/config.yml`; do not copy or depend on the core `config.yml`.
- Build all first-party extensions from this folder with `mvn -DskipTests package`; the build exports individual extension jars and `fpp-extensions-bundle.jar` directly to workspace `builds/` (`D:\Coding and AI Projects\FPP Plugin\builds`).
- Do not copy or move extension jars into `fake-player-plugin/build/extensions/`; `builds/` is the final extension output location.
- Do not add extension-specific config to core. For restart-safe per-bot state, use the FPP API's generic extension data methods instead of bespoke storage. Core DB schema v24 also preserves selected first-party extension fields so uninstall/reinstall does not erase saved bot settings.
