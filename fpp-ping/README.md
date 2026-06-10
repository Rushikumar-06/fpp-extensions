# FPP Ping

FakePlayerPlugin extension that restores `/fpp ping` outside of the core plugin.

## Build

```bash
cd fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-ping
mvn package
```

The extension jar is copied to:

```text
builds/fpp-ping.jar
```

Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

## Command

```text
/fpp ping [<bot>|--count <n>] [--ping <ms>|--random|--reset]
```

Permissions:

- `fpp.ping`
- `fpp.ping.set`
- `fpp.ping.random`
- `fpp.ping.bulk`
