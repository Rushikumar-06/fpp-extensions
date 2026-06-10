# AGENTS.md - fpp-skin

Owns bot skin changes.

Keep here:
- `FppSkinExtension`
- `/fpp skin`
- skin lookup/cache/config resources
- any NameMC/Mojang skin resolution or fallback behavior

Refresh notes:
- `SkinManager` owns the actual profile mutation and viewer refresh through core `FakePlayerManager.refreshSkinForAll`.
- Keep `/fpp skin` from running a second custom packet refresh after `SkinManager` succeeds; duplicate remove/add sequences can race stale profile data.
- Core refresh follows the SkinsRestorer pattern: mutate the underlying NMS `GameProfile`, force skin parts, hide/show visible viewers, then resend player-info/entity packets as fallback.

Core should not expose skin commands or automatically change bot skins. If core needs a neutral profile at spawn time, avoid triggering sessionserver lookups that produce noisy 403 warnings for offline/generated UUIDs.

Random skin assignment belongs to this extension path only. Core spawn must not invent random skin names; only assign a random fallback when skin handling is active and the bot has no saved or requested skin.

Custom URL skins must persist their resolved texture/signature through core DB schema v24 so they survive restarts without requiring another URL fetch.

## Current Integration Notes
- Addon commands should implement `FppAddonCommand.getIcon()` and are shown only in the core help GUI `ADDONS` category.
- Extension settings items should return useful `getValue()` text; core displays that value directly in extension GUI categories.
- Build through `../fpp-extensions` with `cmd /c "..\\fake-player-plugin\\gradlew.bat -p . build"` from `fpp-extensions/`, or `cmd /c "fake-player-plugin\\gradlew.bat -p fpp-extensions build"` from the workspace root. This module is exported individually, included in `fpp-spoof.jar`, and final jars are written directly to workspace `builds/`.
- Keep config in this extension's data folder. Skin texture/signature persistence is intentionally stored by core for restart restore and guarded so it is only applied when `FPP-Skin` is loaded.
- Do not bypass normal core fake-player body setup. Core owns NMS spawn correction, spawn-protection teleport blocking, per-tick physics, persistence autosaves, Paper/Folia damage fallback, and cross-world damage/knockback state resets.
