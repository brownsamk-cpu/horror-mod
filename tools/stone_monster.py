"""Textures for the Stone Monster and the Stony Cave (0.9.0), generated as pixel art.
usage: python3 tools/stone_monster.py   (needs Pillow)

Entity texture is 128x128, laid out to match StoneMonsterModel's boxes:
  head 16x16x16 @ (0,0)   brow 18x4x4 @ (64,0)   shoulder rock 8x6x8 @ (64,8)
  body 24x34x14 @ (0,32)  leg 10x30x10 @ (76,32) arm 10x36x10 @ (0,80)  fist 12x10x12 @ (40,80)
The glowing red eyes are a separate _eyes texture (drawn with the eyes layer so they glow in the dark)."""
import os, random
from PIL import Image
A = "src/main/resources/assets/horrormod/textures"
rnd = random.Random(2609)

BOXES = {  # name: (u, v, w, h, d)
    "head": (0, 0, 16, 16, 16), "brow": (64, 0, 18, 4, 4), "rock": (64, 8, 8, 6, 8),
    "body": (0, 32, 24, 34, 14), "leg": (76, 32, 10, 30, 10), "arm": (0, 80, 10, 36, 10), "fist": (40, 80, 12, 10, 12),
}

def stone(r):
    base = rnd.choice([(118, 118, 122), (104, 104, 110), (128, 126, 124), (96, 96, 102), (112, 110, 108)])
    if r < 0.06:
        return (70, 70, 76, 255)  # crack
    if r < 0.09:
        return (78, 104, 64, 255)  # moss speck
    return base + (255,)

def box_region(u, v, w, h, d):
    return [(u + d, v, w, d), (u + d + w, v, w, d), (u, v + d, d, h), (u + d, v + d, w, h), (u + d + w, v + d, d, h), (u + d + w + d, v + d, w, h)]

def save(img, path):
    os.makedirs(os.path.dirname(A + "/" + path), exist_ok=True)
    img.save(A + "/" + path)

img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
for name, (u, v, w, h, d) in BOXES.items():
    for (x0, y0, ww, hh) in box_region(u, v, w, h, d):
        for y in range(y0, y0 + hh):
            for x in range(x0, x0 + ww):
                img.putpixel((x, y), stone(rnd.random()))
        # darker outline so the boulders read as chunky rocks
        for x in range(x0, x0 + ww):
            for y in (y0, y0 + hh - 1):
                c = img.getpixel((x, y)); img.putpixel((x, y), (c[0] - 22, c[1] - 22, c[2] - 22, 255))
# face (front of head is at (16,16)-(32,32)): dark eye sockets and a jagged frown
F = (16, 16)
for (ex, ey) in [(3, 6), (4, 6), (5, 6), (3, 7), (4, 7), (5, 7), (10, 6), (11, 6), (12, 6), (10, 7), (11, 7), (12, 7)]:
    img.putpixel((F[0] + ex, F[1] + ey), (28, 24, 26, 255))
for (mx, my) in [(4, 12), (5, 11), (6, 12), (7, 11), (8, 12), (9, 11), (10, 12), (11, 11)]:
    img.putpixel((F[0] + mx, F[1] + my), (34, 30, 32, 255))
# a few glowing-red crack lines on the chest (body front at (14,46)), spooky but not gory
for (cx, cy) in [(10, 8), (11, 9), (11, 10), (12, 11), (13, 11), (13, 12)]:
    img.putpixel((14 + cx, 46 + cy), (60, 40, 40, 255))
save(img, "entity/stone_monster.png")

eyes = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
for (ex, ey, c) in [(3, 6, (255, 40, 30)), (4, 6, (255, 90, 60)), (5, 6, (255, 40, 30)), (3, 7, (200, 20, 20)), (4, 7, (255, 40, 30)), (5, 7, (200, 20, 20)),
                    (10, 6, (255, 40, 30)), (11, 6, (255, 90, 60)), (12, 6, (255, 40, 30)), (10, 7, (200, 20, 20)), (11, 7, (255, 40, 30)), (12, 7, (200, 20, 20))]:
    eyes.putpixel((F[0] + ex, F[1] + ey), c + (255,))
for (cx, cy) in [(10, 8), (11, 9), (11, 10), (12, 11), (13, 11), (13, 12)]:
    eyes.putpixel((14 + cx, 46 + cy), (150, 30, 20, 255))  # faint red glow in the chest cracks
save(eyes, "entity/stone_monster_eyes.png")

def draw(path, palette, pixels):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(pixels):
        for x, ch in enumerate(row):
            if ch in palette:
                im.putpixel((x, y), palette[ch])
    save(im, path)

G = {"#": (120, 120, 126, 255), "d": (78, 78, 86, 255), "l": (160, 160, 166, 255), "R": (230, 40, 30, 255), "m": (80, 110, 60, 255)}
# Stony Cave: a gray rocky hill with a dark cave mouth and two red eyes peeking out
draw("item/stony_cave.png", {**G, "k": (20, 18, 22, 255)}, [
    "................",
    "......####......",
    "....##l####.....",
    "...#l######d....",
    "..#########dd...",
    "..##m####l###...",
    ".####dkkkkd###..",
    ".###dkkkkkkd##..",
    ".###kkRkkRkk##d.",
    "####kkkkkkkk###.",
    "###dkkkkkkkkd###",
    "##m#kkkkkkkk#m##",
    "####kkkkkkkk####",
    "dddddddddddddddd",
    "................",
    "................",
])
# Stone Key: a chunky gray stone key with a green moss speck
draw("item/stone_key.png", G, [
    "................",
    "....dddd........",
    "...d####d.......",
    "..d#l..##d......",
    "..d#....#d......",
    "..d#....#d......",
    "..d##..m#d......",
    "...d####d.......",
    "....d##d........",
    ".....##.........",
    ".....##dd.......",
    ".....##.........",
    ".....##ddd......",
    ".....##.........",
    ".....dd.........",
    "................",
])
# Stone Rock: a lumpy little rock
draw("item/stone_rock.png", G, [
    "................",
    "................",
    "................",
    "................",
    "......dddd......",
    ".....d#l##d.....",
    "....d#l####d....",
    "....d######d....",
    "....d###m##d....",
    "....d######d....",
    ".....d####d.....",
    "......dddd......",
    "................",
    "................",
    "................",
    "................",
])
# Stone Monster spawn egg: stone-gray egg with red eye spots
draw("item/stone_monster_spawn_egg.png", G, [
    "................",
    "......dddd......",
    ".....d#l##d.....",
    "....d#l####d....",
    "....d######d....",
    "...d########d...",
    "...d#RR##RR#d...",
    "...d########d...",
    "...d###m####d...",
    "...d########d...",
    "...d##l#####d...",
    "....d######d....",
    "....d######d....",
    ".....d####d.....",
    "......dddd......",
    "................",
])
# Stone Door block: heavy stone slab with a keyhole and rivets
draw("block/stone_door.png", {**G, "k": (20, 18, 22, 255)}, [
    "dddddddddddddddd",
    "d#l##########l#d",
    "d#dddddddddddd#d",
    "d#d##########d#d",
    "d#d#l########d#d",
    "d#d##########d#d",
    "d#d#####kk###d#d",
    "d#d####kkkk##d#d",
    "d#d####kkkk##d#d",
    "d#d#####kk###d#d",
    "d#d#####kk###d#d",
    "d#d##########d#d",
    "d#d##m#######d#d",
    "d#dddddddddddd#d",
    "d#l##########l#d",
    "dddddddddddddddd",
])
print("ok")
