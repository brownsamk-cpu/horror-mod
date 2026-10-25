"""Super Phantom (horrormod:super_phantom). Defines the Super Phantom as DATA: the entity texture (box layout matches
SuperPhantomModel.java), the glowing-eyes texture, the spawn egg, and an ORIGINAL synthesized spooky whoosh
(played when it snatches an item). Darker purple/black than a vanilla phantom, big glowing eyes, not gory.
usage: python3 tools/super_phantom.py   (needs numpy, Pillow, ffmpeg with libvorbis)"""
import os, random, subprocess, wave
import numpy as np
from PIL import Image
A = "src/main/resources/assets/horrormod"
rnd = random.Random(67)

# --- palette: deep night purple and black, a lighter violet edge, glowing cyan-white eyes, magic violet spots ---
BODY = (52, 26, 82)
BODY_D = (24, 12, 40)
BODY_L = (88, 52, 130)
WING = (40, 20, 64)
WING_EDGE = (120, 70, 170)
BONE = (70, 40, 105)            # wing "finger" stripes (soft violet, not bones/gore)
SPOT = (170, 110, 230)          # little magic sparkles on the back
EYE = (120, 255, 240)           # glowing eyes
EYE_CORE = (235, 255, 255)

# front face of the head, 7x3, as data: . body, E eye, W eye core
FACE = ["WE...EW", "EE...EE", "......."]
KEY = {"E": EYE, "W": EYE_CORE}

BOXES = {  # name: (u, v, w, h, d) -- same layout as SuperPhantomModel
    "head": (0, 0, 7, 3, 5),
    "body": (0, 8, 5, 3, 9),
    "tail_base": (3, 20, 3, 2, 6),
    "tail_tip": (4, 29, 1, 1, 6),
    "wing_base": (23, 12, 6, 2, 9),
    "wing_tip": (16, 24, 13, 1, 9),
}

def faces(u, v, w, h, d):
    return [("top", u + d, v, w, d), ("bottom", u + d + w, v, w, d), ("right", u, v + d, d, h),
            ("front", u + d, v + d, w, h), ("left", u + d + w, v + d, d, h), ("back", u + 2 * d + w, v + d, w, h)]

def jitter(c, k=7):
    j = rnd.randint(-k, k)
    return tuple(max(0, min(255, x + j)) for x in c) + (255,)

def body(name, x, y, fw, fh):
    if name == "top" and (x * 3 + y * 5) % 11 == 0:
        return jitter(SPOT, 10)
    if name == "bottom":
        return jitter(BODY_D)
    return jitter(BODY_L if y == 0 else BODY)

def head(name, x, y, fw, fh):
    if name == "front" and FACE[y][x] in KEY:
        return KEY[FACE[y][x]] + (255,)
    return body(name, x, y, fw, fh)

def wing(name, x, y, fw, fh):
    if name in ("top", "bottom"):
        if y == fh - 1:
            return jitter(WING_EDGE)                       # glowing-ish trailing edge
        if x % 4 == 1:
            return jitter(BONE)
        return jitter(WING if name == "top" else BODY_D)
    return jitter(WING_EDGE)

def tail(name, x, y, fw, fh):
    return jitter(BODY_D if name == "bottom" else WING)

img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
for box, fn in (("body", body), ("tail_base", tail), ("tail_tip", tail), ("wing_base", wing), ("wing_tip", wing), ("head", head)):
    for name, x0, y0, fw, fh in faces(*BOXES[box]):
        for y in range(fh):
            for x in range(fw):
                img.putpixel((x0 + x, y0 + y), fn(name, x, y, fw, fh))
os.makedirs(A + "/textures/entity", exist_ok=True)
img.save(A + "/textures/entity/super_phantom.png")

# glowing eyes layer (drawn full-bright, like vanilla phantom eyes): only the eye pixels
eyes = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
u, v, w, h, d = BOXES["head"]
for y in range(h):
    for x in range(w):
        if FACE[y][x] in KEY:
            eyes.putpixel((u + d + x, v + d + y), KEY[FACE[y][x]] + (255,))
eyes.save(A + "/textures/entity/super_phantom_eyes.png")

# --- spawn egg: dark purple egg with violet speckles, two glowing eyes, and a sparkle ---
egg = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for y in range(16):
    for x in range(16):
        dx, dy = (x - 7.5) / 5.6, (y - 8.6) / (7.0 if y > 8 else 7.6)
        r = dx * dx + dy * dy
        if r <= 1.0:
            c = BODY_D if r > 0.75 else (BODY_L if x < 7 and y < 7 else BODY)
            if (x * 7 + y * 3) % 13 == 0 and r <= 0.75:
                c = SPOT
            if (x, y) in ((5, 8), (6, 8), (9, 8), (10, 8)):
                c = EYE
            if (x, y) in ((5, 8), (10, 8)):
                c = EYE_CORE
            egg.putpixel((x, y), c + (255,))
for p in ((12, 2), (13, 1), (13, 3), (14, 2)):   # tiny sparkle = a stolen shiny thing
    egg.putpixel(p, (255, 240, 140, 255))
os.makedirs(A + "/textures/item", exist_ok=True)
egg.save(A + "/textures/item/super_phantom_spawn_egg.png")
os.makedirs("previews", exist_ok=True)
egg.resize((256, 256), Image.NEAREST).save("previews/super_phantom_spawn_egg_big.png")

# --- original 'snatch' sound: a breathy spooky whoosh sweeping down, then a quick little rising swish ---
SR = 44100
dur = 1.3
t = np.arange(int(SR * dur)) / SR
rng = np.random.default_rng(67)
noise = rng.normal(0, 1, t.size)
# whoosh: noise through a moving one-pole lowpass (cutoff sweeps high -> low), shaped by a swell
cut = 3000 * np.exp(-2.5 * t) + 250
alpha = 1 - np.exp(-2 * np.pi * cut / SR)
wh = np.zeros_like(noise); acc = 0.0
for i in range(t.size):
    acc += alpha[i] * (noise[i] - acc); wh[i] = acc
wh *= np.sin(np.pi * np.clip(t / 0.8, 0, 1)) ** 2 * 0.9
swish = (t >= 0.5) * np.exp(-9 * np.clip(t - 0.5, 0, None)) * np.sin(2 * np.pi * np.cumsum(500 + 900 * np.clip(t - 0.5, 0, None)) / SR)
sig = wh / np.abs(wh).max() * 0.8 + 0.25 * swish
sig = (sig / np.abs(sig).max() * 0.6 * 32767).astype(np.int16)
with wave.open("/tmp/super_phantom_snatch.wav", "wb") as wf:
    wf.setnchannels(1); wf.setsampwidth(2); wf.setframerate(SR); wf.writeframes(sig.tobytes())
os.makedirs(A + "/sounds/entity", exist_ok=True)
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", "/tmp/super_phantom_snatch.wav", "-ac", "1", "-c:a", "libvorbis", "-q:a", "5",
                A + "/sounds/entity/super_phantom_snatch.ogg"], check=True)
print("wrote super_phantom.png, super_phantom_eyes.png, super_phantom_spawn_egg.png, super_phantom_snatch.ogg")
