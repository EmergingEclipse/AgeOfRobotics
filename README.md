# Age of Robotics

A Minecraft mod: build a portal to a wrecked machine world, power a base from scrap, and assemble
modular robots that mine, farm, build and haul for you.

- **Minecraft version:** 1.21.1
- **Mod loader:** NeoForge 21.1.250
- **Java:** 21
- **Mod id / namespace:** `ageofrobotics`

Feature tracker: [GitHub issues](https://github.com/EmergingEclipse/AgeOfRobotics/issues),
grouped into the `MVP`, `Release 1` and `Release 2` milestones.

## Project layout

| Path | Contents |
| --- | --- |
| `core/` | Loader-independent gameplay model (robot parts and stats, power maths, tiering). No Minecraft imports, so it is unit tested directly. |
| `mod/` | The NeoForge mod: registries, block entities, entities, world generation. Depends on `core` and folds it into the shipped jar. |
| `config/` | Checkstyle rules shared by both modules. |
| `.husky/` | Git hooks: secret scanning, file-size limits, branch-name and lint checks. |

Base packages under `mod/src/main/java/com/modpackswork/ageofrobotics/` mirror the issue labels:
`item`, `block`, `dimension`, `robot`, `power`.

## Building

Requires a JDK (21 is downloaded automatically by the Gradle toolchain if it is missing).

```bash
./gradlew build
```

Other useful tasks:

```bash
./gradlew :core:test      # fast unit tests, no Minecraft download
./gradlew :mod:runClient  # launch a dev client with the mod loaded
./gradlew :mod:runServer  # launch a dev server
./gradlew spotlessApply   # format Java sources
```

The first `:mod` build downloads and decompiles Minecraft through NeoForm and takes a while;
later builds reuse the Gradle cache.

The built jar lands in `mod/build/libs/`.

## CI

`azure-pipelines.yml` runs the unit tests, builds the jar, publishes JUnit results and uploads the
jar as a pipeline artifact on every push to `main` and every pull request into it.

## Contributing

Run `npm install` once to enable the git hooks. Commits are linted (Markdown, YAML, Java
formatting, secret scanning); pushes to `main` are blocked — open a pull request from a feature
branch instead.
