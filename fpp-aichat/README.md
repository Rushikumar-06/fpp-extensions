# FPP-AIChat

Provider-backed AI chat for FakePlayerPlugin.

Build the core plugin first, then this extension:

```bash
cd ../../fake-player-plugin
mvn package
cd ../fpp-extensions/fpp-aichat
mvn package
```

Build output is written to `builds/fpp-aichat.jar`. Install it into:

```text
plugins/FakePlayerPlugin/extensions/
```

On first load the extension creates:

```text
plugins/FakePlayerPlugin/extensions/FPP-AIChat/config.yml
plugins/FakePlayerPlugin/extensions/FPP-AIChat/secrets.yml
plugins/FakePlayerPlugin/extensions/FPP-AIChat/personalities/
```
