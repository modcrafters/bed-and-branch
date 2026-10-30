# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

To release, rename **Unreleased** to the version (for example `## 0.1.0`), use that section as the CurseForge changelog, and start a new empty **Unreleased** above it.

## Unreleased

- Berry obelisks are wooden cages. Two can be stacked, and the first sits on farmland. Sweet berries grow inside as a branched bush. A full bush keeps growing into the cage above, and the berries show through the openings. Picking takes only the ripe berries. The bush stays and fruits again.
- Vine trellises are wooden screens. Three can be stacked. A vine planted on any piece of a stack grows from the bottom and climbs to the top. A full ground trellis can spread to an empty ground trellis beside it. Picking takes the vines from the one you clicked and every trellis above it. The bottom keeps a short vine. The ones above go bare.
- Nether farmland is hoed netherrack on soul sand. Lava nearby fertilizes it. Weeping vines grow on a trellis there, and only while it stays fertilized. They do not need daylight. Ordinary vines will not grow on it.
- The forward debug stick no longer harvests a finished plant. On a finished vine it starts the empty trellis above. On a full berry bush it starts the empty cage above, or sets the next berries.

- Crop sticks are four corner poles. A click on the soil between them still uses the stick, and sticks work on vanilla farmland as well as tilled soil.
- Plants on a stick grow as 3D models: wheat, carrots, potatoes, beets, melons, pumpkins, torchflowers, and tomatoes. Off a stick, the vanilla crops stay as they are.
- Beets on a stick have five stages. The last two wither the leaves while the root shows, and each stage takes a little longer than wheat.
- Melons and pumpkins on a stick keep their fruit inside the poles. Harvesting drops one melon or one pumpkin.
- A ripe torchflower on a stick glows.
- Tomatoes on a stick grow their fruit along the vine, and they still need loam.
- Tomato seeds planted with no stick grow Wild Tomatoes, on vanilla farmland or on any tilled soil. Wild Tomatoes are a flat, full-height crop. Tomatoes and tomato seeds have their own icons.
- Muddy tilled soil has trenches that hold water when the soil is moist.
- The creative tab includes two debug sticks that move a plant forward or back one growth stage.
- On NeoForge 1.21.1, tilled soil, items, and plants show their textures. The soil and crop-stick recipes work, and an unripe tomato drops seeds.
- NeoForge 1.21.1 is supported, as well as Fabric and NeoForge 26.3. The soil tooltip uses Jade on both. Jade's plugin code is still named Waila; the old Waila mod is not required.
- Crop sticks stay planted. Seeds grow between the poles, harvesting takes the fruit, and breaking the stick drops the stick and the crop.
- Tilled soil is named for the block under it: Sandy, Loam, Peat, Muddy, or Flooded Mud.
- Each of those soils has its own tilled top.
- Mud stays dry until a water bucket is poured on it, which makes it the flooded soil. An empty bucket takes the water back, and the water does not spill.
- With Jade installed, looking at a plant shows its stage, the soil, whether that soil suits it, and a fertility score. The mod still runs if Jade is not installed.
