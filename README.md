# Bed & Branch

Minecraft farming mod: crop beds, soils (including flooded mud for rice), crop sticks, bushes, fruit trees, and **grafting**. No kitchen / food recipes — those belong in a separate companion mod.

Funny sibling to [Barnyard Romance](https://github.com/modcrafters/barnyard-romance) (animal breeding). Same spirit, dirt instead of livestock.

## Crafting and soil preparation

| Result | Ingredients | Layout |
|---|---|---|
| 1 loam | 2 dirt + 1 clay ball | Shapeless |
| 1 peat | 1 dirt + 1 moss block | Shapeless |
| 1 crop stick | 2 sticks | One above the other |
| 1 vine trellis | 7 sticks | `S S` / `SSS` / `S S` |
| 1 berry obelisk | 6 sticks + 2 planks | `SPS` / `S S` / `SPS` |

In the patterns, `S` is a wooden stick and `P` is any plank. Recipes appear in the recipe book after collecting the relevant materials. Wooden sticks also accept the common `c:rods/wooden` tag when supplied by another mod.

Place dirt above loam, peat, sand/red sand, or mud, then hoe the dirt to prepare the corresponding farmland. Use a water bucket on muddy farmland to flood it for rice; the bucket comes back empty. Nether farmland is prepared by hoeing netherrack above soul sand. These tilled blocks are prepared in the world rather than at a crafting table.

## Abandoned rice farms

New chunks can contain small abandoned farms in plains, sunflower plains, swamps, rivers, and meadows. Rice grows on flooded muddy farmland above vanilla mud. The plots contain a mix of growth stages, empty crop sticks, irrigation, and a little recoverable loam. Harvest rice to get the seeds for your own farm.

| Layout | Footprint | Placement |
|---|---|---|
| Pocket paddy | 5 × 5 | Small, mostly level clearing |
| Split beds | 7 × 7 | Two beds with a central path |
| Creekside plot | 9 × 7 | Shallow water immediately beside a level bank |
| Terraced patch | 9 × 9 | An existing one-block rise between two beds |
| Ruined farmstead | 9 × 9 | Level ground around a small ruined shed |

Every layout tries four rotations. Placement checks the entire footprint and surrounding edge before writing blocks: natural ground must support the beds, trees and existing builds are left alone, and cliffs, deep water, and excessive leveling are rejected. No column is raised or cut by more than one block, and at least three quarters of the land footprint must already match the layout. Terraces are not built as raised platforms on flat ground.

These small ruins generate as world features. For creative testing, use `/place feature bed_and_branch:pocket_paddy` on suitable terrain; replace `pocket_paddy` with `split_beds`, `creekside_plot`, `terraced_patch`, or `ruined_farmstead`. The same terrain checks apply to manual placement. They are not registered for `/locate structure`.

Datapacks can change eligible biomes through `bed_and_branch:has_abandoned_rice_farm` and rarity through the five `worldgen/placed_feature` files. Rarity is an attempt rate per eligible chunk, not a guarantee: terrain checks can reject the attempt. Existing chunks are not modified.

`gradlew :neoforge:1.21.1:runGameTestServer` runs placement regressions in an isolated test world: all five layouts, surviving rice beds, and rejection without partial placement. CI runs these alongside the unit tests.

## Import and run in IntelliJ IDEA

IDEA 2025.2 or newer is required for Java 25.

1. Open this folder (the one that contains `gradlew.bat`) as a project.
2. Set the Project SDK and the Gradle JVM to Java 25:
   - `File > Project Structure > Project SDK`
   - `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`
3. Install the **Stonecutter Dev** plugin (optional; it highlights the `//?` version comments).
4. Refresh the Gradle project.
5. Run one of these configurations:
   - **Fabric Client** or **Fabric Server** (Gradle run configurations)
   - **NeoForge Client** or **NeoForge Server** (Application run configurations)

### How the build is laid out

The build uses [Stonecutter](https://stonecutter.kikugie.dev) so one codebase can build for several Minecraft versions:

- The common code is in `src/`. Fabric code is in `fabric/src`, and NeoForge code is in `neoforge/src`.
- Each Minecraft version is its own Gradle project: `:26.3` and `:1.21.1` (common), `:fabric:26.3`, `:neoforge:26.3` and `:neoforge:1.21.1`. Fabric is 26.3 only. Each one's `gradle.properties` under `versions/<mc>` holds that version's Minecraft, Java and loader versions.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The IDE always shows one version, the *active* one.
- Switch with the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state, and CI checks this.

`gradlew collectJars` builds every loader jar and copies them to `build/libs/<mod version>/`. Each jar's own build output is under `<loader>/versions/<mc>/build/libs`. The common jars are not mods you install.

`gradlew :26.3:test` and `gradlew :1.21.1:test` run the unit tests. CI (`.github/workflows/ci.yml`) builds Fabric and NeoForge 26.3 and NeoForge 1.21.1 on every pull request. The `all-versions` check passes only when every version passed; it is the check for a ruleset on `main` to require.

Looking at a plant can show its soil through [Jade](https://modrinth.com/mod/jade). Jade is optional on both versions. Its plugin interfaces are still named Waila; that is Jade's API, not a second mod.
