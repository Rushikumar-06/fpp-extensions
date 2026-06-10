# FPP Waypoints

Addon-owned named waypoint routes and bot patrols for FakePlayerPlugin.

Build:

```bash
cd fake-player-plugin
mvn package -DskipTests
cd ../fpp-extensions/fpp-waypoints
mvn package -DskipTests
```

Output:

```text
builds/fpp-waypoints.jar
```

Commands:

```text
/fpp waypoint add <route>
/fpp waypoint create <route>
/fpp waypoint remove <route> <index>
/fpp waypoint delete <route>
/fpp waypoint clear <route>
/fpp waypoint list [route]
/fpp waypoint patrol <bot|all> <route> [--random]
/fpp waypoint stop <bot|all>
```
