# Horror Mod

A spooky-fun (Halloween, not scary) Fabric mod for Minecraft 26.3, built one step at a time.

## Step 1: the Ghost (first called the Spooky Spirit)
- `horrormod:spooky_spirit`: a small, **clear, see-through** ghost.
- It floats slowly, drifts around, bobs up and down, and looks at you.
- Now and then it says a soft "wooo" (subtitle: "Ghost goes wooo").
- It is passive: it never attacks or hurts anyone.
- It has a spawn egg and spawns rarely, only in the dark (at night or in caves).

The texture, spawn egg, icon, and sound are defined as data in `tools/spooky_spirit.py`.

Build: `./gradlew build` (runs the game tests). Fabric Loader 0.19.5, Fabric API 0.163.0+26.3, Java 25.

## Step 2 (0.2.0): Super Phantom

`horrormod:super_phantom` - "like a phantom, but worse". A big, dark purple/black phantom with glowing cyan eyes that flies
and swoops slowly around at night. It **never** attacks or does damage. Its trick: when it swoops within about 2 blocks of a
survival/adventure player, it snatches **one** item from a random hotbar/main-inventory slot (never your armor or offhand),
with a whoosh, purple sparkles, smoke, and the subtitle "Super Phantom snatches an item". Then it has to wait about 30 seconds.
It keeps what it stole (it won't despawn while holding your things) and drops **everything** it took when it is defeated.

- Rare: only at night under the open sky. Spawn egg: "Super Phantom Spawn Egg" (next to the Phantom egg in Creative).
- Texture, glowing eyes, spawn egg, and the whoosh sound are all data in `tools/super_phantom.py`.
- Previews: `previews/super_phantom_*.png`.
