"""Textures for Castle Level 7, defined as DATA: 16x16 pixel maps + palettes.
usage: python3 tools/castle_level_seven_textures.py   (needs Pillow)"""
import os
from PIL import Image
A = "src/main/resources/assets/horrormod/textures"

def draw(path, palette, pixels, preview=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(pixels):
        for x, ch in enumerate(row):
            if ch in palette:
                img.putpixel((x, y), palette[ch])
    os.makedirs(os.path.dirname(A + "/" + path), exist_ok=True)
    img.save(A + "/" + path)
    if preview:
        os.makedirs("previews", exist_ok=True)
        img.resize((256, 256), Image.NEAREST).save("previews/" + preview)

# Electra Core: a glowing WHITE square with a faint pale-blue electric sparkle (June's idea)
draw("block/electra_core.png", {
    "W": (255, 255, 255, 255), "w": (240, 244, 250, 255), "e": (230, 236, 246, 255), "s": (190, 225, 255, 255), "S": (150, 205, 255, 255),
}, [
    "eeeeeeeeeeeeeeee",
    "eWWWWWWWWWWWWWWe",
    "eWwwwwwwwwwwwwWe",
    "eWwWWWWWWWWWWwWe",
    "eWwWWWsWWWWWWwWe",
    "eWwWWsSsWWWWWwWe",
    "eWwWWWsWWWWWWwWe",
    "eWwWWWWWWWWWWwWe",
    "eWwWWWWWWWsWWwWe",
    "eWwWWWWWWsSsWwWe",
    "eWwWWWWWWWsWWwWe",
    "eWwWWsWWWWWWWwWe",
    "eWwWWWWWWWWWWwWe",
    "eWwwwwwwwwwwwwWe",
    "eWWWWWWWWWWWWWWe",
    "eeeeeeeeeeeeeeee",
], "electra_core_texture_big.png")

# Grapple Target: purple-and-white bullseye
draw("block/grapple_target.png", {
    "p": (110, 50, 170, 255), "P": (160, 90, 220, 255), "W": (250, 250, 250, 255), "y": (255, 210, 60, 255),
}, [
    "pppppppppppppppp",
    "pPPPPPPPPPPPPPPp",
    "pPWWWWWWWWWWWWPp",
    "pPWPPPPPPPPPPWPp",
    "pPWPWWWWWWWWPWPp",
    "pPWPWPPPPPPWPWPp",
    "pPWPWPWWWWPWPWPp",
    "pPWPWPWyyWPWPWPp",
    "pPWPWPWyyWPWPWPp",
    "pPWPWPWWWWPWPWPp",
    "pPWPWPPPPPPWPWPp",
    "pPWPWWWWWWWWPWPp",
    "pPWPPPPPPPPPPWPp",
    "pPWWWWWWWWWWWWPp",
    "pPPPPPPPPPPPPPPp",
    "pppppppppppppppp",
])

# Level 7 Start Pad: green glowing pad with a white star (you come back here if you fall)
draw("block/level_seven_start.png", {
    "g": (30, 120, 60, 255), "G": (70, 200, 100, 255), "W": (240, 255, 240, 255),
}, [
    "gggggggggggggggg",
    "gGGGGGGGGGGGGGGg",
    "gGGGGGGWWGGGGGGg",
    "gGGGGGGWWGGGGGGg",
    "gGGGGGWWWWGGGGGg",
    "gGWWWWWWWWWWWWGg",
    "gGGWWWWWWWWWWGGg",
    "gGGGWWWWWWWWGGGg",
    "gGGGGWWWWWWGGGGg",
    "gGGGGWWWWWWGGGGg",
    "gGGGWWWGGWWWGGGg",
    "gGGGWWGGGGWWGGGg",
    "gGGWWGGGGGGWWGGg",
    "gGGGGGGGGGGGGGGg",
    "gGGGGGGGGGGGGGGg",
    "gggggggggggggggg",
])

# Grappling Hook: a three-pronged iron hook with a rope
draw("item/grappling_hook.png", {
    "o": (40, 40, 48, 255), "i": (170, 172, 182, 255), "I": (235, 236, 244, 255),
    "r": (120, 84, 44, 255), "R": (176, 132, 76, 255),
}, [
    "................",
    "..o....o....o...",
    ".oIo..oIo..oIo..",
    ".oio..oio..oio..",
    "..oio.oio.oio...",
    "...oioiioio.....",
    "....oiIIio......",
    ".....oiio.......",
    ".....oiio.......",
    "......oo........",
    ".....orRo.......",
    "....orRo........",
    "...orRo.........",
    "..orRo..........",
    ".orRo...........",
    "..oo............",
], "grappling_hook_texture_big.png")

# Castle Level 7 placer: a tiny castle with a white Electra Core flag
draw("item/castle_level_seven.png", {
    "o": (30, 30, 36, 255), "s": (120, 120, 128, 255), "S": (160, 160, 168, 255), "d": (70, 70, 80, 255),
    "y": (250, 200, 80, 255), "W": (255, 255, 255, 255), "b": (110, 200, 255, 255), "r": (230, 90, 20, 255),
}, [
    ".......o........",
    ".......oWWW.....",
    ".......oWbW.....",
    ".......oWWW.....",
    ".......o........",
    ".o.o.oSSSo.o.o..",
    ".ososoSsSosoSo..",
    ".oSSSoSySoSSSo..",
    ".oSySoSsSoSySo..",
    ".oSssSSsSSssSo..",
    ".oSsSSSSSSSsSo..",
    ".oSSSSo..oSSSo..",
    ".oSdSSo..oSdSo..",
    ".oSSSSorroSSSo..",
    ".oooooooooooo...",
    "................",
], "castle_level_seven_texture_big.png")
print("wrote castle level 7 textures")
