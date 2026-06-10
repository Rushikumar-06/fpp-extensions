# FPP LuckPerms

FakePlayerPlugin extension that restores `/fpp lpinfo` and `/fpp rank` outside of the core plugin.

## Build

```bash
cd fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-luckperms
mvn package
```

The extension jar is copied to:

```text
builds/fpp-luckperms.jar
```

Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

## Commands

```text
/fpp lpinfo
/fpp rank <bot> <group|clear>
/fpp rank random <group> [num]
/fpp rank list
```
