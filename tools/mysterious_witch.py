"""Mysterious Witch (horrormod:mysterious_witch). Defines her as DATA: the entity texture (box layout matches
MysteriousWitchModel.java), the glowing-eyes layer, her 4 mod potions (names, colors, durations) and their effect icons,
a little bat particle, the spawn egg, and an ORIGINAL synthesized cackle.
Purple and teal robes, a crooked hat with little yellow stars, glowing green eyes. Not the vanilla witch, nothing gory.
usage: python3 tools/mysterious_witch.py   (needs numpy, Pillow, ffmpeg with libvorbis)"""
import json, os, random, subprocess, wave
import numpy as np
from PIL import Image
A = "src/main/resources/assets/horrormod"
rnd = random.Random(1031)

# --- her 4 potions, as data (read by MysteriousPotions.java at startup) ---
POTIONS = [
    # id, display name, color, duration in ticks (drinkable; splash scales down by distance)
    {"id": "bouncy", "name": "Bouncy Potion", "effect": "Bouncy", "color": "#4FD8FF", "duration": 600},
    {"id": "glow", "name": "Glow Potion", "effect": "Glowy", "color": "#FFE45C", "duration": 600},
    {"id": "bat", "name": "Bat Potion", "effect": "Bat Swarm", "color": "#6B3FA0", "duration": 400},
    {"id": "slime_feet", "name": "Slime Feet Potion", "effect": "Slime Feet", "color": "#7CE05A", "duration": 600},
]
os.makedirs("src/main/resources/data/horrormod", exist_ok=True)
with open("src/main/resources/data/horrormod/mysterious_potions.json", "w") as f:
    json.dump(POTIONS, f, indent=1); f.write("\n")

# --- palette ---
SKIN = (176, 214, 190)          # pale minty skin (vanilla witch is tan)
SKIN_D = (140, 180, 158)
ROBE = (92, 44, 140)            # purple robe
ROBE_D = (62, 28, 98)
TEAL = (32, 150, 150)           # teal trim and sash
TEAL_L = (70, 200, 190)
HAT = (40, 22, 70)              # deep purple hat
HAT_L = (66, 40, 104)
STAR = (255, 226, 90)
EYE = (80, 255, 110)            # glowing green
EYE_CORE = (210, 255, 210)
MOUTH = (90, 60, 90)
HAIR = (30, 120, 120)           # teal hair

FACE = [  # head front 8x8: H hair, E eye, W eye core, M mouth, . skin
    "HHHHHHHH",
    "H......H",
    "........",
    ".WE..EW.",
    "........",
    "........",
    "..MMMM..",
    "........",
]
FKEY = {"H": HAIR, "E": EYE, "W": EYE_CORE, "M": MOUTH}

BOXES = {  # name: (u, v, w, h, d) -- same layout as MysteriousWitchModel (texture 128x64)
    "head": (0, 0, 8, 8, 8),
    "nose": (32, 0, 2, 3, 2),
    "body": (0, 16, 8, 13, 6),
    "arm": (32, 16, 3, 10, 3),
    "skirt": (0, 36, 10, 11, 8),
    "brim": (64, 0, 12, 1, 12),
    "hat_mid": (64, 16, 7, 4, 7),
    "hat_tip": (64, 32, 4, 4, 4),
    "hat_tip2": (96, 32, 2, 3, 2),
}

def faces(u, v, w, h, d):
    return [("top", u + d, v, w, d), ("bottom", u + d + w, v, w, d), ("right", u, v + d, d, h),
            ("front", u + d, v + d, w, h), ("left", u + d + w, v + d, d, h), ("back", u + 2 * d + w, v + d, w, h)]

def j(c, k=6):
    q = rnd.randint(-k, k)
    return tuple(max(0, min(255, x + q)) for x in c) + (255,)

def head(n, x, y, fw, fh):
    if n == "front":
        return FKEY[FACE[y][x]] + (255,) if FACE[y][x] in FKEY else j(SKIN, 4)
    if n == "top" or (n in ("back", "left", "right") and y < 6):
        return j(HAIR)
    return j(SKIN_D if n == "bottom" else SKIN, 4)

def nose(n, x, y, fw, fh):
    return j(SKIN_D, 3)

def robe(n, x, y, fw, fh):
    if n in ("front", "back") and y in (7, 8):
        return j(TEAL_L if x % 3 else STAR)             # teal sash with a little star buckle
    if n == "front" and x in (3, 4) and y < 7:
        return j(TEAL)                                    # teal trim down the front
    return j(ROBE_D if (x + y) % 4 == 0 else ROBE)

def arm(n, x, y, fw, fh):
    if y >= fh - 2:
        return j(SKIN, 4) if y == fh - 1 else j(TEAL)   # teal cuff, minty hand
    return j(ROBE)

def skirt(n, x, y, fw, fh):
    if n not in ("top", "bottom") and y >= fh - 2:
        return j(TEAL if y == fh - 2 else TEAL_L)       # teal hem
    if n in ("front", "back", "left", "right") and (x * 5 + y * 3) % 17 == 0:
        return j(STAR)                                    # tiny stars on the robe
    return j(ROBE_D if n == "bottom" else ROBE)

def hat(n, x, y, fw, fh):
    if (x * 7 + y * 11) % 13 == 0 and n != "bottom":
        return j(STAR, 2)                                 # little stars on the hat
    return j(HAT_L if y == 0 else HAT)

def brim(n, x, y, fw, fh):
    if n in ("top", "bottom") and (x in (0, fw - 1) or y in (0, fh - 1)):
        return j(TEAL)                                    # teal brim edge
    return hat(n, x, y, fw, fh)

img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
for box, fn in (("head", head), ("nose", nose), ("body", robe), ("arm", arm), ("skirt", skirt), ("brim", brim),
                ("hat_mid", hat), ("hat_tip", hat), ("hat_tip2", hat)):
    for name, x0, y0, fw, fh in faces(*BOXES[box]):
        for y in range(fh):
            for x in range(fw):
                img.putpixel((x0 + x, y0 + y), fn(name, x, y, fw, fh))
os.makedirs(A + "/textures/entity", exist_ok=True)
img.save(A + "/textures/entity/mysterious_witch.png")

eyes = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
u, v, w, h, d = BOXES["head"]
for y in range(8):
    for x in range(8):
        if FACE[y][x] in "EW":
            eyes.putpixel((u + d + x, v + d + y), FKEY[FACE[y][x]] + (255,))
eyes.save(A + "/textures/entity/mysterious_witch_eyes.png")

# --- effect icons (18x18), one per potion: a little picture in the potion's color ---
def hexrgb(s):
    return tuple(int(s[i:i + 2], 16) for i in (1, 3, 5))
ICONS = {
    "bouncy": ["....XXXX....", "...X....X...", "..X......X..", "..X......X..", "...X....X...", "....XXXX....",
               "............", "..X......X..", "...X....X...", "....X..X....", ".....XX.....", "XXXXXXXXXXXX"],
    "glow": [".....XX.....", ".....XX.....", "..X..XX..X..", "...XXXXXX...", "..XXXXXXXX..", "XXXXXXXXXXXX",
             "XXXXXXXXXXXX", "..XXXXXXXX..", "...XXXXXX...", "..X..XX..X..", ".....XX.....", ".....XX....."],
    "bat": ["............", "............", "X....XX....X", "XX..XXXX..XX", "XXXXXXXXXXXX", "XXXXXXXXXXXX",
            ".XXXXXXXXXX.", "..XX.XX.XX..", "...X....X...", "............", "............", "............"],
    "slime_feet": ["............", "...XXXXXX...", "..XXXXXXXX..", ".XXX.XX.XXX.", ".XXX.XX.XXX.", ".XXXXXXXXXX.",
                   ".XXXXXXXXXX.", "..XXXXXXXX..", "............", "X.X.X.X.X.X.", ".X.X.X.X.X.X", "............"],
}
os.makedirs(A + "/textures/mob_effect", exist_ok=True)
for p in POTIONS:
    c = hexrgb(p["color"])
    icon = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    for y, row in enumerate(ICONS[p["id"]]):
        for x, ch in enumerate(row):
            if ch == "X":
                icon.putpixel((3 + x, 3 + y), c + (255,))
    # dark outline so it reads on any background
    out = icon.copy()
    for y in range(18):
        for x in range(18):
            if icon.getpixel((x, y))[3] == 0 and any(0 <= x + dx < 18 and 0 <= y + dy < 18 and icon.getpixel((x + dx, y + dy))[3]
                                                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out.putpixel((x, y), (30, 15, 45, 255))
    out.save(A + f"/textures/mob_effect/{p['id']}.png")

# --- little bat particle (8x8) ---
BAT = ["........", "X......X", "XX.XX.XX", "XXXXXXXX", ".XXXXXX.", "..X..X..", "........", "........"]
bat = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
for y, row in enumerate(BAT):
    for x, ch in enumerate(row):
        if ch == "X":
            bat.putpixel((x, y), (40, 20, 60, 255))
bat.putpixel((3, 3), (255, 230, 120, 255)); bat.putpixel((4, 3), (255, 230, 120, 255))   # tiny eyes
os.makedirs(A + "/textures/particle", exist_ok=True)
bat.save(A + "/textures/particle/bat.png")
os.makedirs(A + "/particles", exist_ok=True)
with open(A + "/particles/bat.json", "w") as f:
    json.dump({"textures": ["horrormod:bat"]}, f); f.write("\n")

# --- spawn egg: purple egg with teal speckles, two green eyes and a little star ---
egg = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for y in range(16):
    for x in range(16):
        dx, dy = (x - 7.5) / 5.6, (y - 8.6) / (7.0 if y > 8 else 7.6)
        r = dx * dx + dy * dy
        if r <= 1.0:
            c = ROBE_D if r > 0.75 else (ROBE if not (x < 7 and y < 7) else (124, 70, 176))
            if (x * 5 + y * 7) % 11 == 0 and r <= 0.75:
                c = TEAL_L
            if (x, y) in ((5, 8), (6, 8), (9, 8), (10, 8)):
                c = EYE
            egg.putpixel((x, y), c + (255,))
for p in ((7, 2), (8, 1), (8, 2), (8, 3), (9, 2)):
    egg.putpixel(p, STAR + (255,))
os.makedirs(A + "/textures/item", exist_ok=True)
egg.save(A + "/textures/item/mysterious_witch_spawn_egg.png")
os.makedirs("previews", exist_ok=True)
egg.resize((256, 256), Image.NEAREST).save("previews/mysterious_witch_spawn_egg_big.png")

# --- original cackle: 'heh-heh-heh-heeeh' - a buzzy pulse voice through two 'eh' formants, pitch wobbling up ---
SR = 44100
def syllable(f0, dur, vib=0.0):
    t = np.arange(int(SR * dur)) / SR
    f = f0 * (1 + 0.08 * np.sin(2 * np.pi * 7 * t) * vib) * (1 + 0.15 * t / dur)
    ph = np.cumsum(f) / SR
    src = 2 * (ph % 1.0) - 1.0                                    # sawtooth 'voice'
    out = np.zeros_like(t)
    for fc, bw, g in ((560, 90, 1.0), (1850, 140, 0.6), (2600, 200, 0.3)):   # 'eh' formants
        r = np.exp(-np.pi * bw / SR); w0 = 2 * np.pi * fc / SR
        a1, a2 = -2 * r * np.cos(w0), r * r
        y1 = y2 = 0.0; y = np.zeros_like(t)
        for i in range(t.size):
            yi = src[i] - a1 * y1 - a2 * y2; y[i] = yi; y2, y1 = y1, yi
        out += g * y / np.abs(y).max()
    env = np.minimum(1, t / 0.012) * np.exp(-3.0 * t / dur)
    breath = np.random.default_rng(int(f0)).normal(0, 0.15, t.size)
    return (out + breath) * env
parts = []
for i, (f0, d) in enumerate(((300, 0.13), (330, 0.13), (360, 0.13), (395, 0.14), (440, 0.45))):
    parts.append(syllable(f0, d, vib=1.0 if i == 4 else 0.3))
    parts.append(np.zeros(int(SR * 0.04)))
sig = np.concatenate(parts + [np.zeros(int(SR * 0.1))])
sig = (sig / np.abs(sig).max() * 0.6 * 32767).astype(np.int16)
with wave.open("/tmp/mysterious_witch_cackle.wav", "wb") as wf:
    wf.setnchannels(1); wf.setsampwidth(2); wf.setframerate(SR); wf.writeframes(sig.tobytes())
os.makedirs(A + "/sounds/entity", exist_ok=True)
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", "/tmp/mysterious_witch_cackle.wav", "-ac", "1", "-c:a", "libvorbis", "-q:a", "5",
                A + "/sounds/entity/mysterious_witch_cackle.ogg"], check=True)
print("wrote mysterious_witch textures, potions json, effect icons, bat particle, egg, cackle")
