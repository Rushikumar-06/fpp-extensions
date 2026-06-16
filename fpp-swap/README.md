# FPP Swap

FakePlayerPlugin extension that restores `/fpp swap` and the Swap settings tab outside of the core plugin.

## Build

```bash
cd fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-swap
mvn package
```

The extension jar is copied to:

```text
builds/fpp-swap.jar
```

Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

## Command

```text
/fpp swap [on|off|status|now <bot>|list|info <bot>|time <session|absence> <minSeconds> <maxSeconds>]
```

Examples:

```text
/fpp swap time session 120 600
/fpp swap time absence 30 180
```

The extension also registers a Swap tab in `/fpp settings`.
