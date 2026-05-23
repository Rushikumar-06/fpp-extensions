# Changelog

All notable changes to FPP Extensions will be documented in this file.

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
| 1.1.0 | 2026-05-23 | Bulk operations, config fixes, Gradle migration |
| 1.0.0 | 2026-05-22 | Initial release |
