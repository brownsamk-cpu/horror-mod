"""Spirit item (horrormod:spirit), dropped by the Ghost. Defined as DATA: a 16x16 pixel map + palette.
Whitish gray wisp with a little purple sparkle.
usage: python3 tools/spirit.py   (needs Pillow)"""
import os
from PIL import Image
A = "src/main/resources/assets/horrormod"

PALETTE = {
    "o": (120, 124, 138, 255),   # soft gray outline
    "g": (196, 198, 206, 255),   # light gray
    "w": (236, 238, 242, 255),   # whitish
    "W": (252, 252, 255, 255),   # bright white shine
    "p": (170, 90, 230, 255),    # purple sparkle
    "P": (225, 175, 255, 255),   # light purple sparkle core
}
PIXELS = [
    "................",
    ".............p..",
    "......oo....pPp.",
    ".....owwo....p..",
    "....owWwgo......",
    "...owWwwwgo.....",
    "...owwwwwgo.....",
    "..owwwwpwwgo....",
    "..owwwpPpwgo....",
    "..owwwwpwwgo....",
    "...owwwwwgo.....",
    "....oggwggo..p..",
    ".....ogogo..pPp.",
    "..p...o.o....p..",
    ".pPp............",
    "..p.............",
]
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for y, row in enumerate(PIXELS):
    for x, ch in enumerate(row):
        if ch in PALETTE:
            img.putpixel((x, y), PALETTE[ch])
os.makedirs(A + "/textures/item", exist_ok=True)
img.save(A + "/textures/item/spirit.png")
os.makedirs("previews", exist_ok=True)
img.resize((256, 256), Image.NEAREST).save("previews/spirit_texture_big.png")
print("wrote spirit.png")
