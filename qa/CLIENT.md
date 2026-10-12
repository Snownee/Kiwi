# Minecraft 26.3 client smoke test

`client_smoke.py` validates production JARs with Fabric Loader 0.19.5 in a fresh
profile under `build/qa-client/`. It never uses an existing world or modifies a
launcher profile. The Java `user.home` is isolated too, so Kiwi's global-options
feature cannot change real user preferences.

Prerequisites: Java 25, Python 3, a working graphical session, cached Fabric
Loader dependencies, and a JSON launch audit containing a `command` array for a
working vanilla Minecraft 26.3 client. That command must use `-cp` with existing
JARs, `net.minecraft.client.main.Main`, and standard version/assets/profile
arguments. The harness substitutes Fabric's entrypoint, its own game directory,
and test fixture. It rejects Java agents and directory classpath entries. No
production class is replaced by a test class.

Prepare a directory containing Fabric API 0.161.0+26.3, Cloth Config 26.3.159,
and Mod Menu 21.0.0-beta.1. Add the production Snow Real Magic JAR to the same
directory to test its client integration and configuration screen as well.

```sh
python3 qa/client_smoke.py \
  --jar build/libs/Kiwi-mc26.3-Fabric-26.0.20-port.1+26.3.jar \
  --mods /path/to/dependency-jars \
  --launch-audit /path/to/vanilla-26.3-launch-audit.json \
  --java /path/to/java25/bin/java
```

The default display backend is Wayland; override `SDL_VIDEODRIVER` for a working
alternative. The fixture waits for resource loading, explicitly loads inventory,
effects, entity-rendering and fog classes to check mixin application, opens the
Kiwi Cloth Config screen, and lets it render for 40 ticks. When Snow Real Magic
is installed, it repeats this for that mod's configuration screen, then exits
normally. It does not create a game world or manually inspect gameplay visuals.

Each run records JAR checksums, the launch command, console output, exit status,
and a machine-readable result. Offline-account HTTP 401 and Realms errors, plus
a missing optional system `libflite` narrator library, are reported separately
as environment diagnostics. Other ERROR/FATAL messages fail the check.

## Optional synchronized snow-world check

Pass `--server 127.0.0.1:PORT` to join a separately running disposable SRM test
server. The server must contain at least four snow-covered blocks with block
entities in the region `(-8,118,-8)` through `(8,124,8)`. The SRM server fixture
leaves a fence, wall, slab, and stairs at x=0,2,4,6, y=120, z=0, with a floor at
y=119. Teleport the test player `KiwiClientQA` to `3 120 6` on join. The fixture
looks toward the grid, waits 120 client world ticks, checks that at least four
snow block entities synchronized, and saves a screenshot before opening the
configuration screens. Use this mode only with a fresh/copy test world and an
offline localhost test server. A passing run logs `KIWI_CLIENT_QA_WORLD_PASS`.
This is a rendering/synchronization smoke check, not exhaustive gameplay QA.
