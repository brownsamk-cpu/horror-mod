"""Textures for Castle Level 6, defined as DATA: 16x16 pixel maps + palettes.
usage: python3 tools/castle_textures.py   (needs Pillow)"""
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

# Level 7 Key: spooky gold key with a little skull-ish bow and a purple gem
draw("item/level_seven_key.png", {
    "o": (92, 58, 12, 255), "y": (214, 160, 32, 255), "Y": (250, 214, 84, 255), "W": (255, 246, 190, 255),
    "k": (40, 26, 8, 255), "p": (150, 70, 210, 255),
}, [
    "................",
    "...oooo.........",
    "..oYYYyo........",
    ".oYWkYkyo.......",
    ".oYYYYYyo.......",
    ".oYkkkYyo.......",
    "..oyyyyo........",
    "...oyyo.........",
    "....oypo........",
    ".....oyyo.......",
    "......oyyo......",
    ".......oyyo.oo..",
    "........oyyoYo..",
    ".........oyyyo..",
    "..........oYYo..",
    "...........oo...",
], "level_seven_key_texture_big.png")

# Zip Line Hook: an iron hook on a short rope handle
draw("item/zip_line_hook.png", {
    "o": (40, 40, 48, 255), "i": (170, 172, 182, 255), "I": (230, 232, 240, 255),
    "r": (120, 84, 44, 255), "R": (168, 124, 70, 255),
}, [
    "................",
    "......ooooo.....",
    ".....oIiiiio....",
    "....oIo...oio...",
    "....oio....oio..",
    "....oio....oio..",
    ".....o.....oio..",
    "..........oio...",
    ".........oio....",
    "........oio.....",
    ".......orRo.....",
    "......orRo......",
    ".....orRo.......",
    "....orRo........",
    "...orRo.........",
    "....oo..........",
], "zip_line_hook_texture_big.png")

# Castle Level 6 placer: a tiny spooky castle with a purple 6 flag
draw("item/castle_level_six.png", {
    "o": (30, 30, 36, 255), "s": (120, 120, 128, 255), "S": (160, 160, 168, 255), "d": (70, 70, 80, 255),
    "y": (250, 200, 80, 255), "p": (150, 70, 210, 255), "P": (220, 170, 255, 255), "b": (90, 60, 30, 255),
}, [
    ".......o........",
    ".......oppp.....",
    ".......opPp.....",
    ".......oppp.....",
    ".......o........",
    ".o.o.oSSSo.o.o..",
    ".ososoSsSosoSo..",
    ".oSSSoSySoSSSo..",
    ".oSySoSsSoSySo..",
    ".oSssSSsSSssSo..",
    ".oSsSSSSSSSsSo..",
    ".oSSSSobboSSSo..",
    ".oSdSSobboSdSo..",
    ".oSSSSobboSSSo..",
    ".oooooooooooo...",
    "................",
], "castle_level_six_texture_big.png")

# Level 7 Gate: dark iron bars with a gold keyhole
draw("block/level_seven_gate.png", {
    ".": (24, 22, 30, 255), "i": (88, 88, 98, 255), "I": (140, 140, 152, 255), "y": (230, 180, 50, 255), "k": (10, 8, 6, 255),
}, [
    "IIIIIIIIIIIIIIII",
    "i.Ii..Ii..Ii..Ii",
    "i.Ii..Ii..Ii..Ii",
    "i.Ii..Ii..Ii..Ii",
    "IIIIIIIIIIIIIIII",
    "i.Ii..Ii..Ii..Ii",
    "i.Ii.yyyy.Ii..Ii",
    "i.Ii.ykky.Ii..Ii",
    "i.Ii.ykky.Ii..Ii",
    "i.Ii.yyky.Ii..Ii",
    "i.Ii.yyyy.Ii..Ii",
    "IIIIIIIIIIIIIIII",
    "i.Ii..Ii..Ii..Ii",
    "i.Ii..Ii..Ii..Ii",
    "i.Ii..Ii..Ii..Ii",
    "IIIIIIIIIIIIIIII",
])

# Stray Blast Pad: purple with a glowing orange arrow pointing forward (top), striped sides
draw("block/stray_blast_pad_top.png", {
    ".": (70, 30, 110, 255), ",": (96, 44, 146, 255), "o": (255, 150, 30, 255), "O": (255, 220, 120, 255),
}, [
    ",,,,,,,,,,,,,,,,",
    ",..............,",
    ",......oo......,",
    ",.....oOOo.....,",
    ",....oOOOOo....,",
    ",...oOOOOOOo...,",
    ",..oOOOOOOOOo..,",
    ",.....oOOo.....,",
    ",.....oOOo.....,",
    ",.....oOOo.....,",
    ",.....oOOo.....,",
    ",.....oOOo.....,",
    ",.....oooo.....,",
    ",..............,",
    ",..............,",
    ",,,,,,,,,,,,,,,,",
])
draw("block/stray_blast_pad_side.png", {
    ".": (70, 30, 110, 255), "o": (255, 150, 30, 255), "d": (40, 40, 48, 255),
}, [
    "oooooooooooooooo",
    "................",
    "o...o...o...o...",
    ".o...o...o...o..",
    "..o...o...o...o.",
    "...o...o...o...o",
    "................",
    "dddddddddddddddd",
] + ["dddddddddddddddd"] * 8)
print("wrote castle textures")
