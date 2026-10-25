"""Spooky Spirit (horrormod:spooky_spirit). Defines the spirit as DATA: the see-through entity texture
(box layout matches SpookySpiritModel.java), the spawn egg, the mod icon, and an ORIGINAL synthesized soft 'wooo'.
CLEAR = every body pixel has partial alpha, so blocks show through it in game.
usage: python3 tools/spooky_spirit.py   (needs numpy, Pillow, ffmpeg with libvorbis)"""
import os, random, subprocess, wave
import numpy as np
from PIL import Image
A = "src/main/resources/assets/horrormod"
rnd = random.Random(31)

# --- palette (r, g, b, alpha): misty see-through white-blue, darker misty edges, solid-ish friendly face ---
MIST = (225, 238, 255, 120)
MIST_L = (245, 250, 255, 140)
MIST_D = (190, 210, 240, 95)
EYE = (40, 30, 70, 230)         # big dark cartoon eyes
EYE_SHINE = (255, 255, 255, 235)
MOUTH = (60, 40, 90, 210)       # a little round 'o' for wooo
CHEEK = (255, 190, 220, 150)

# front face of the head, 8x8, as data: . mist, E eye, S shine, O mouth, C cheek
FACE = [
    "........",
    "........",
    ".SE..SE.",
    ".EE..EE.",
    "C......C",
    "...OO...",
    "...OO...",
    "........",
]
KEY = {"E": EYE, "S": EYE_SHINE, "O": MOUTH, "C": CHEEK}

BOXES = {  # name: (u, v, w, h, d)
    "head": (0, 0, 8, 8, 8),
    "body": (0, 16, 7, 6, 6),
    "tail": (0, 32, 5, 4, 4),
    "arm": (32, 16, 2, 5, 2),
}

def faces(u, v, w, h, d):
    return [("top", u + d, v, w, d), ("bottom", u + d + w, v, w, d), ("right", u, v + d, d, h),
            ("front", u + d, v + d, w, h), ("left", u + d + w, v + d, d, h), ("back", u + 2 * d + w, v + d, w, h)]

def mist(name, x, y, fw, fh):
    base = MIST_L if (name == "top" or (x + 2 * y) % 5 == 0) else MIST
    if x == 0 or x == fw - 1:
        base = MIST_D
    k = rnd.randint(-6, 6)
    return tuple(max(0, min(255, c + k)) for c in base[:3]) + (base[3] + rnd.randint(-8, 8),)

def head(name, x, y, fw, fh):
    if name == "front" and FACE[y][x] in KEY:
        return KEY[FACE[y][x]]
    return mist(name, x, y, fw, fh)

def tail(name, x, y, fw, fh):
    if name not in ("top", "bottom") and y == fh - 1 and x % 2 == 1:   # wavy, wispy bottom edge
        return (0, 0, 0, 0)
    c = mist(name, x, y, fw, fh)
    return c[:3] + (max(40, c[3] - 15 * y),)                            # fades out toward the tip

img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
for box, fn in (("head", head), ("body", mist), ("tail", tail), ("arm", mist)):
    for name, x0, y0, fw, fh in faces(*BOXES[box]):
        for y in range(fh):
            for x in range(fw):
                img.putpixel((x0 + x, y0 + y), fn(name, x, y, fw, fh))
os.makedirs(A + "/textures/entity", exist_ok=True)
img.save(A + "/textures/entity/spooky_spirit.png")

# --- spawn egg: misty egg with two dark eyes and an 'o' mouth (egg itself is solid so it reads well in the hotbar) ---
egg = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for y in range(16):
    for x in range(16):
        dx, dy = (x - 7.5) / 5.6, (y - 8.6) / (7.0 if y > 8 else 7.6)
        r = dx * dx + dy * dy
        if r <= 1.0:
            c = (150, 170, 210) if r > 0.75 else ((245, 250, 255) if x < 7 and y < 8 else (215, 228, 250))
            if (x, y) in ((5, 7), (5, 8), (9, 7), (9, 8)):
                c = EYE[:3]
            if (x, y) in ((7, 11), (8, 11)):
                c = MOUTH[:3]
            egg.putpixel((x, y), c + (255,))
os.makedirs(A + "/textures/item", exist_ok=True)
egg.save(A + "/textures/item/spooky_spirit_spawn_egg.png")
egg.resize((128, 128), Image.NEAREST).save(A + "/icon.png")

# --- original 'wooo': a soft, low, wavering sine glide with vibrato and a breathy hush (not a scream) ---
SR = 44100
dur = 1.4
t = np.arange(int(SR * dur)) / SR
f = 300 + 90 * np.sin(np.pi * t / dur) + 12 * np.sin(2 * np.pi * 5.0 * t)    # glide up then down, gentle vibrato
ph = 2 * np.pi * np.cumsum(f) / SR
tone = np.sin(ph) + 0.25 * np.sin(2 * ph) + 0.06 * np.sin(3 * ph)
env = np.sin(np.pi * t / dur) ** 1.8
breath = np.random.default_rng(31).normal(0, 0.05, t.size) * env
sig = np.concatenate([(tone * env + breath) * 0.5, np.zeros(int(SR * 0.1))])
sig = (sig / np.abs(sig).max() * 0.6 * 32767).astype(np.int16)
with wave.open("/tmp/spooky_spirit_wooo.wav", "wb") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(sig.tobytes())
os.makedirs(A + "/sounds/entity", exist_ok=True)
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", "/tmp/spooky_spirit_wooo.wav", "-ac", "1", "-c:a", "libvorbis", "-q:a", "5",
                A + "/sounds/entity/spooky_spirit_wooo.ogg"], check=True)
print("wrote spooky_spirit.png, spooky_spirit_spawn_egg.png, icon.png, spooky_spirit_wooo.ogg")
