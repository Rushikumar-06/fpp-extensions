# FPP Skin

FakePlayerPlugin extension that restores `/fpp skin` outside of the core plugin.

## Build

```bash
cd fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-skin
mvn package
```

The extension jar is copied to:

```text
builds/fpp-skin.jar
```

Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

## Command

```text
/fpp skin <bot> <username|reset>
/fpp skin <bot> --url <url>
```

Resolved skins are saved as texture/signature data and re-applied on restart. Random fallback skins are assigned only by this extension and only when the bot has no saved or requested skin.
