# FPP Chat

FakePlayerPlugin extension that restores `/fpp chat` outside of the core plugin.

## Build

```bash
cd fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-chat
mvn package
```

The extension jar is copied to:

```text
builds/fpp-chat.jar
```

Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

## Command

```text
/fpp chat [on|off|status|all]
/fpp chat all <on|off|status|say <message>|mute [seconds]>
/fpp chat <bot> [on|off|status|info|say <message>|mute [seconds]]
```
