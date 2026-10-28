# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

To release, rename **Unreleased** to the version (for example `## 0.1.0`), use that section as the CurseForge changelog, and start a new empty **Unreleased** above it.

## Unreleased

- NeoForge 1.21.1 is supported, as well as Fabric and NeoForge 26.3. The soil tooltip uses Jade on both. Jade's plugin code is still named Waila; the old Waila mod is not required.
- Crop sticks stay planted. Seeds grow between the poles, harvesting takes the fruit, and breaking the stick drops the stick and the crop.
- Tilled soil is named for the block under it: Sandy, Loam, Peat, Muddy, or Flooded Mud.
- Each of those soils has its own tilled top.
- Mud stays dry until a water bucket is poured on it, which makes it the flooded soil. An empty bucket takes the water back, and the water does not spill.
- With Jade installed, looking at a plant shows its stage, the soil, whether that soil suits it, and a fertility score. The mod still runs if Jade is not installed.
