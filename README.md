# FPP Extensions

Extended functionality for Fake Player Plugin (FPP) on Minecraft servers.

## Version

**Current:** 1.1.1

## Requirements

- Minecraft 1.21+
- Paper/Spigot 1.21+
- Java 21
- Fake Player Plugin 1.6.6.12.1+
- LuckPerms (optional, for the LuckPerms extension)

## Installation

### Option 1: Full Pack (Recommended)

1. Build the spoof pack from the workspace root:
   ```powershell
   cmd /c "fake-player-plugin\gradlew.bat -p fpp-extensions build"
   ```

2. Copy `builds/fpp-spoof.jar` to your server's `plugins/FakePlayerPlugin/extensions/` folder

3. Restart your server

### Option 2: Individual Extensions

Copy individual `.jar` files from workspace `builds/` to your server's `plugins/FakePlayerPlugin/extensions/` folder.

## Extensions Included

| Extension | Description | Commands | Permissions |
|-----------|-------------|----------|-------------|
| **fpp-aichat** | AI-powered chat for bots using LLM APIs | N/A | `fpp.aichat` |
| **fpp-chat** | Bot chat with cooldowns and anti-spam | N/A | `fpp.chat` |
| **fpp-luckperms** | LuckPerms integration for bots | N/A | `fpp.luckperms` |
| **fpp-pathfinder** | First-party pathfinding service and settings | N/A | `fpp.pathfinder` |
| **fpp-ping** | Show or spoof bot ping values | `/fpp ping` | `fpp.ping`, `fpp.ping.set` |
| **fpp-skin** | Manage bot skins from MCHead/NameMC | `/fpp skin` | `fpp.skin`, `fpp.skin.set` |
| **fpp-swap** | Bot swap scheduling and rejoin behavior | `/fpp swap` | `fpp.swap` |
| **fpp-waypoints** | Bot waypoint/pathfinding system | `/fpp waypoint` | `fpp.waypoints` |

### Excluded Extensions

| Extension | Reason |
|-----------|--------|
| fpp-command | Removed from this extension pack |
| fpp-groups | Removed from this extension pack |
| fpp-list | Removed from this extension pack |
| fpp-nametag | Removed from this extension pack |
| fpp-peaks | Removed from this extension pack |

## Commands

### Ping Extension

```
/fpp ping <bot>                    # Show bot ping
/fpp ping <bot> --ping <ms>        # Set bot ping
/fpp ping <bot> --random           # Random ping (20-500ms)
/fpp ping <bot> --reset            # Reset to default

/fpp ping --all                    # Show all bot pings
/fpp ping --all --ping <ms>        # Set ping for all bots
/fpp ping --all --random           # Random ping for all bots
/fpp ping --all --reset            # Reset all bot pings

/fpp ping --count <n>              # Show n random bots
/fpp ping --count <n> --ping <ms>  # Set ping for n bots
```

### Skin Extension

```
/fpp skin <bot>                    # Show bot skin info
/fpp skin <bot> --skin <url>       # Set custom skin
/fpp skin <bot> --random           # Random skin
/fpp skin <bot> --reset            # Reset to default

/fpp skin --all                    # Show all bot skins
/fpp skin --all --skin <url>       # Set skin for all bots
/fpp skin --all --random           # Random skins for all bots
/fpp skin --all --reset            # Reset all bot skins
```

## Configuration

Extension configs are generated in `plugins/FakePlayerPlugin/extensions/` on first run.

### Key Settings

**fpp-chat/config.yml**
```yaml
enabled: true
chat-cooldown-ms: 3000
chat-random-delay-ms: 2000
```

**fpp-ping/config.yml**
```yaml
enabled: true
default-ping: 0
```

**fpp-skin/config.yml**
```yaml
enabled: true
skin-source: "mchead"  # or "namemc"
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `fpp.ping` | View ping | true |
| `fpp.ping.set` | Set ping | op |
| `fpp.ping.random` | Random ping | op |
| `fpp.ping.bulk` | Bulk operations | op |
| `fpp.skin` | View skins | true |
| `fpp.skin.set` | Set skins | op |
| `fpp.skin.random` | Random skins | op |
| `fpp.aichat` | AI chat | op |
| `fpp.chat` | Bot chat | op |
| `fpp.luckperms` | LuckPerms integration | op |
| `fpp.pathfinder` | Pathfinding | op |
| `fpp.swap` | Swap management | op |
| `fpp.waypoints` | Waypoints | op |

## Building from Source

```bash
# Build all extensions and fpp-spoof.jar from the workspace root
cmd /c "fake-player-plugin\gradlew.bat -p fpp-extensions build"

# Build specific extension
cmd /c "fake-player-plugin\gradlew.bat -p fpp-extensions :fpp-ping:build :fpp-ping:copyExtension"

# Output locations
builds/fpp-spoof.jar    # Full pack
builds/fpp-*.jar        # Individual extensions
```

## Development

### Project Structure

```
fpp-extensions/
├── fpp-aichat/      # AI chat integration
├── fpp-chat/        # Bot chat system
├── fpp-luckperms/   # LuckPerms integration
├── fpp-pathfinder/  # Pathfinding service
├── fpp-ping/        # Ping spoofing
├── fpp-skin/        # Skin management
├── fpp-swap/        # Swap scheduling
├── fpp-waypoints/   # Waypoint system
└── fpp-spoof/       # Combined pack
```

### Adding New Extensions

1. Create new module in `settings.gradle.kts`
2. Add `build.gradle.kts` with FPP API dependency
3. Implement `FppExtension` interface
4. Add to `settings.gradle.kts` and the root `extensionProjects` list so it is included in `fpp-spoof.jar`

## Known Issues

- **ChatColor deprecation warning:** Uses legacy Bukkit API for compatibility. Safe to ignore.

## Changelog

See [CHANGELOG.md](CHANGELOG.md)

## License

MIT License - See LICENSE file for details

## Support

- Issues: https://github.com/yourusername/fpp-extensions/issues
- Discord: https://discord.gg/WRvfmV24Hh
