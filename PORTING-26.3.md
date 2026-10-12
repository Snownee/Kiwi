# Fabric 26.3 dependency port

This branch starts from upstream `26.1-fabric` commit
`0bf0e28d35454a8cbbb25a5715d2fbdcdad71a81` (Kiwi 26.0.20). It supplies the
Kiwi dependency needed by the Snow! Real Magic Fabric 26.3 port. It is an
unofficial port, not an upstream Kiwi release.

Build with a Java 25 JDK:

```sh
./gradlew build --no-daemon
```

The installable file is
`build/libs/Kiwi-mc26.3-Fabric-26.0.20-port.1+26.3.jar`.
Use Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3. Cloth Config 26.3.159
and Mod Menu 21.0.0-beta.1 supply the optional configuration UI.

The port updates GUI and SDL input access, model submission, predicates,
resource selectors, recipe output delegation, language generation, and block
template construction. `DeferredHolder` extends vanilla's permitted
`Holder.Reference` implementation because `Holder` is now sealed. Deferred
lookups still delegate to the registered holder. Block-entity item data is
applied after successful client placement through `placeBlock`, replacing
the removed override point.

Source compatibility changes for mod authors:

- The removed Fabric tool-conversion, composting, and villager-food registry
  helpers are no longer exposed through `Platform`. Minecraft 26.3 supplies
  data-driven components/definitions for these operations.
- Vanilla removed the block codec registry. Kiwi now registers constructors
  for its bundled block templates; extensions must register their own codecs
  through `BlockCodecs.register`.
- Block-state-provider, placement-modifier, and structure-placement codecs
  need explicit registry keys instead of inference from removed type classes.
- Legacy tree-grower fields are translated to weighted feature lists.

Validation completed on 2026-09-29:

- Gradle build, access-widener validation, and all 52 existing JUnit tests pass.
- The production client JAR loads resources and inventory/effects/rendering
  mixins, opens Kiwi's Cloth Config screen, renders it for 40 ticks, and exits
  normally. The same checks also pass with Snow Real Magic installed, including
  its client mixin targets and a separately rendered SRM configuration screen.
- A production client joined a disposable server with SRM, synchronized four
  snow block entities, and rendered them for 120 client ticks. The captured
  screenshot was inspected: snow-covered fence, wall, slab, and stairs rendered
  correctly. Both configuration screens then rendered and the client exited 0.
- Offline-account authentication and missing optional narrator
  library diagnostics are recorded separately.
- The final production JAR passes isolated dedicated-server startup, resource
  reload, and graceful shutdown with Fabric API and Cloth Config; no logged
  errors. The harness records exact input hashes and launch arguments.

Run the server check using a directory containing only the selected dependency
JARs and an installed Minecraft/Fabric cache:

```sh
python3 qa/server_smoke.py --jar build/libs/Kiwi-mc26.3-Fabric-26.0.20-port.1+26.3.jar --mods /path/to/dependencies --java /path/to/jdk25/bin/java
```

See [qa/CLIENT.md](qa/CLIENT.md) for the separate production-client fixture,
launch-audit prerequisites, exact arguments, and coverage.

Each server check creates a disposable localhost world under ignored `build/qa-run/`.
It does not change an existing gameplay profile or world. Test fixtures are
development files and are excluded from the production JAR.

The wider low-code customization system, third-party Kiwi modules, cosmetics,
JEI integration, and every combination of optional mini-mods have not received
exhaustive gameplay validation. This port does not claim binary compatibility
with mods built for the removed 26.1 APIs.
