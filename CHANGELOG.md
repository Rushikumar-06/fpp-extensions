# Changelog

All notable changes to FPP Extensions will be documented in this file.

## [Unreleased] - 2026-06-09

### Changed
- Updated the `fpp-spoof.jar` bundle module list to exclude removed extensions that no longer exist in this workspace
- Updated extension build documentation to use the core Gradle wrapper and workspace `builds/` outputs

### Removed
- Removed stale bundle references to **fpp-command**, **fpp-groups**, **fpp-list**, **fpp-nametag**, and **fpp-peaks**

---

## [1.1.1] - 2026-06-05

### Added
- **fpp-pathfinder**: Restored as a first-party extension with a pathfinding service controller
- **fpp-pathfinder**: Added global and per-bot pathfinding settings tabs for navigation options
- **fpp-swap**: Restored as a first-party extension with `/fpp swap` command support and swap settings
- **fpp-skin**: Added `/fpp spawn --skin <username|url>` command extension support
- **fpp-skin**: Added extension-owned skin data persistence for saved bot skins
- **fpp-luckperms**: Added extension-owned bot display decoration service for LuckPerms prefixes and suffixes
- **fpp-nametag**: Added extension-owned NameTag service integration for nick and skin isolation

### Changed
- Moved more addon behavior behind public FPP extension APIs, including commands, command extensions, services, and settings tabs
- Updated all first-party extension module versions from `1.1.0` to `1.1.1`
- Added `fpp-spoof.jar` output containing the individual first-party extension jars
- Extension build output now copies individual jars and the bundle directly to workspace `builds/`

### Fixed
- **fpp-skin**: Custom and URL-applied skins now persist across bot saves, despawns, and respawns
- **fpp-skin**: Saved skins are reapplied after spawn so restored bots keep their configured appearance
- **fpp-list**: Tab-list team syncing now handles bot spawn, despawn, player joins, and restoration timing more safely
- **fpp-swap**: Swap scheduling reloads cleanly when settings change and cancels pending swap tasks on disable
- **fpp-peaks**: Peak-hours sleeping bot state can restore from the database when restart persistence is enabled

### Technical
- Added extension-owned config registration for pathfinding, swap, and peak-hours settings
- Added extension command icons for core help/addon GUI display
- Improved cleanup on extension disable by unregistering commands, services, settings tabs, listeners, and scheduled work

---

## [1.1.0] - 2026-05-23

### Added
- **fpp-ping**: Added `--all` flag for bulk ping operations on all bots
- **fpp-skin**: Added `--all` flag for bulk skin operations on all bots
- Bulk command support for `--ping`, `--random`, and `--reset` options
- Enhanced tab completion for bulk operations

### Changed
- **fpp-ping**: Migrated to Adventure Component API (reduces deprecation warnings)
- **fpp-chat**: Config defaults now save properly on first run
- **fpp-list**: Changed `bot-tab-list.enabled` default from `true` to `false`
- Renamed extension pack from `fpp-pack` to `fpp-spoof`
- Updated all module versions from `1.0.0` to `1.1.0`
- Output jar now includes version: `fpp-spoof-1.1.0-all.jar`

### Fixed
- **fpp-chat**: Messages not sending due to config not loading defaults
- **fpp-list**: Tab list interfering with LuckPerms group ordering
- **fpp-ping**: ChatColor deprecation warnings (partial migration to Adventure)
- Null safety improvements across all extensions
- Error handling for invalid command arguments

### Technical
- Converted all modules from Maven (pom.xml) to Gradle (build.gradle.kts)
- Removed all pom.xml files from project
- Added proper dependency management in Gradle
- Improved build configuration and task organization

---

## [1.0.0] - 2026-05-22

### Added
- Initial release with 11 extensions:
  - **fpp-aichat**: AI-powered bot chat using LLM APIs
  - **fpp-chat**: Bot chat with cooldowns and anti-spam
  - **fpp-command**: Execute commands as bots
  - **fpp-groups**: Bot group management
  - **fpp-list**: Advanced player list/tab control
  - **fpp-luckperms**: LuckPerms integration for bots
  - **fpp-nametag**: Custom nametags for bots
  - **fpp-peaks**: Display server TPS and performance
  - **fpp-ping**: Show or spoof bot ping values
  - **fpp-skin**: Manage bot skins from MCHead/NameMC
  - **fpp-waypoints**: Bot waypoint/pathfinding system
- Combined extension pack (`fpp-pack-all.jar`)
- Individual extension builds
- Basic command permissions
- Configuration files for each extension

### Excluded
- **fpp-pathfinder**: Functionality moved to base FPP plugin
- **fpp-swap**: Incompatible with FPP 1.6.6.12.1 API

---

## Version History

| Version | Date | Notes |
|---------|------|-------|
| 1.1.1 | 2026-06-05 | Restored pathfinder/swap extensions, skin persistence, service/settings API integration |
| 1.1.0 | 2026-05-23 | Bulk operations, config fixes, Gradle migration |
| 1.0.0 | 2026-05-22 | Initial release |
