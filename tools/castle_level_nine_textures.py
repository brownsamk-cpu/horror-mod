"""Textures for Castle Level 9, defined as DATA: 16x16 pixel maps + palettes.
usage: python3 tools/castle_level_nine_textures.py   (needs Pillow)"""
import os
from PIL import Image
A = "src/main/resources/assets/horrormod/textures"

def draw(path, palette, pixels):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(pixels):
        for x, ch in enumerate(row):
            if ch in palette:
                img.putpixel((x, y), palette[ch])
    os.makedirs(os.path.dirname(A + "/" + path), exist_ok=True)
    img.save(A + "/" + path)

# Castle Level 9: a stone castle tower with a big gold 9 and a red button
draw("item/castle_level_nine.png", {"#": (120, 120, 128, 255), "d": (70, 70, 80, 255), "G": (250, 200, 40, 255), "R": (220, 40, 40, 255)}, [
    "..d.d.d..d.d.d..",
    "..dddddddddddd..",
    "..#############.",
    "..#####GGG####..",
    "..####G###G###..",
    "..####G###G###..",
    "..#####GGGG###..",
    "..#########G##..",
    "..#########G##..",
    "..####G###G###..",
    "..#####GGG####..",
    "..############..",
    "..#####RR#####..",
    "..####RRRR####..",
    "..############..",
    "..dddddddddddd..",
])
# Level 10 Key: a shiny blue-and-gold key
draw("item/level_ten_key.png", {"G": (250, 200, 40, 255), "g": (190, 140, 20, 255), "B": (60, 140, 255, 255)}, [
    "................",
    "....GGGG........",
    "...GggggG.......",
    "..GgBBBBgG......",
    "..GgB..BgG......",
    "..GgB..BgG......",
    "..GgBBBBgG......",
    "...GggggG.......",
    "....GggG........",
    ".....Gg.........",
    ".....GgGGG......",
    ".....Gg.........",
    ".....GgGG.......",
    ".....Gg.........",
    ".....GgGGG......",
    "................",
])
print("ok")
