"""Castle LEVEL SEVEN, designed by June (6). The whole course is DATA: a 3D grid of letters
(one picture per height layer), written to src/main/resources/data/horrormod/castle/level_seven.json.
The game reads that file and builds the course in front of you when you use the "Castle Level 7" item.

June's design: glowing WHITE square Electra Cores that slide back and forth over a big gap
(stand on one and it carries you), lava and then the void below (falling just sends you back to
the start, no hurting), a Grappling Hook with grapple targets across the gap, and NO cats.

Local directions: x = to your right (0..W-1), z = forward away from you (0..L-1), y = up (0..H-1).
The start floor is layer y=12, so the gap goes 12 blocks down below where you stand.
usage: python3 tools/castle_level_seven.py
"""
import json, os

W, L, H = 13, 42, 21
FLOOR = 12          # top of the start / island / end platforms
KEY = {
    " ": "air", "#": "stone bricks", "d": "deepslate bricks", "t": "deepslate tiles", "c": "cracked stone bricks",
    "m": "mossy stone bricks", "b": "polished blackstone", "G": "gold block", "L": "lantern", "l": "soul lantern",
    "~": "lava (just a look: touching it sends you back to the start, no hurting)",
    "r": "rescue field (invisible: falling into it sends you back to the start)",
    "e": "Electra Core (white glowing moving platform, starts going forward)",
    "T": "grapple target", "S": "Level 7 start pad (where you come back to)",
    "C": "chest with the Grappling Hook", "o": "sign",
}
grid = [[[" "] * W for _ in range(L)] for _ in range(H)]

def put(x, y, z, ch):
    grid[y][z][x] = ch

def box(x0, x1, y0, y1, z0, z1, ch):
    for y in range(y0, y1 + 1):
        for z in range(z0, z1 + 1):
            for x in range(x0, x1 + 1):
                put(x, y, z, ch)

def wall_ch(y, n):
    if y % 5 == 0:
        return "d"
    return "c" if (n * 7 + y * 3) % 11 == 0 else ("m" if (n * 5 + y) % 13 == 0 else "#")

# castle walls left/right all the way down (the gap is a deep pit), back wall with a door, end wall
for z in range(L):
    for y in range(0, H - 1):
        put(0, y, z, wall_ch(y, z)); put(W - 1, y, z, wall_ch(y, z + 3))
for x in range(W):
    for y in range(0, H - 1):
        put(x, y, 0, wall_ch(y, x)); put(x, y, L - 1, wall_ch(y, x + 2))
box(5, 7, FLOOR + 1, FLOOR + 3, 0, 0, " ")             # front door
for z in range(0, L, 2):                                # battlements
    put(0, H - 1, z, "d"); put(W - 1, H - 1, z, "d")
for x in range(0, W, 2):
    put(x, H - 1, 0, "d"); put(x, H - 1, L - 1, "d")

# START platform (z 1..6), solid down to the bottom
box(1, W - 2, 0, FLOOR, 1, 6, "t")
box(1, W - 2, FLOOR, FLOOR, 1, 6, "b")
put(6, FLOOR, 3, "S")
put(3, FLOOR + 1, 2, "C"); put(8, FLOOR + 1, 2, "o")
put(1, FLOOR + 1, 6, "L"); put(W - 2, FLOOR + 1, 6, "l")

# the BIG GAP: z 7..19 and 23..34, with a small island in the middle (z 20..22)
GAP1, GAP2 = (7, 19), (23, 34)
for z0, z1 in (GAP1, GAP2):
    # lava floor at the edges, the void (open air to the very bottom) in the middle
    box(1, 3, 2, 2, z0, z1, "t"); box(1, 3, 3, 3, z0, z1, "~")
    box(9, 11, 2, 2, z0, z1, "t"); box(9, 11, 3, 3, z0, z1, "~")
    box(1, 3, 4, 5, z0, z1, "r"); box(9, 11, 4, 5, z0, z1, "r")    # above the lava
    box(4, 8, 0, 4, z0, z1, "r")                                   # bottom of the void
    box(4, 4, 2, 3, z0, z1, "t"); box(8, 8, 2, 3, z0, z1, "t")     # lava pool rims (so the lava stays put)
# middle island
box(1, W - 2, 0, FLOOR, 20, 22, "t")
box(1, W - 2, FLOOR, FLOOR, 20, 22, "b")
put(1, FLOOR + 1, 21, "l"); put(W - 2, FLOOR + 1, 21, "l")
put(10, FLOOR + 1, 20, "o")
# ELECTRA CORES: 3x3 white squares. Gap 1: one in the middle lane. Gap 2: one on the left lane.
box(5, 7, FLOOR, FLOOR, 7, 9, "e")
box(2, 4, FLOOR, FLOOR, 23, 25, "e")
# GRAPPLE TARGETS floating across the gaps (grapple one and it pulls you on top of it)
for x, z in ((2, 11), (2, 16)):
    put(x, FLOOR, z, "T")
for x, z in ((9, 26), (9, 30)):
    put(x, FLOOR, z, "T")
put(9, FLOOR + 3, 36, "T")                               # one up on the end platform wall side
# END platform (z 35..40) with the LEVEL 8 sign and a gold 8
box(1, W - 2, 0, FLOOR, 35, 40, "t")
box(1, W - 2, FLOOR, FLOOR, 35, 40, "b")
put(9, FLOOR + 1, 36, "#"); put(9, FLOOR + 2, 36, "#")
EIGHT = [".GGG.", ".G.G.", ".GGG.", ".G.G.", ".GGG."]
for r, row in enumerate(EIGHT):
    for c, ch in enumerate(row):
        if ch == "G":
            put(4 + c, FLOOR, 40 - r, "G")
put(6, FLOOR + 1, 37, "o")
put(1, FLOOR + 1, 35, "L"); put(W - 2, FLOOR + 1, 35, "L")
# lanterns on the walls of the pit
for z in range(8, 35, 6):
    for x in (1, W - 2):
        put(x, 9, z, "d"); put(x, 10, z, "l")

SIGNS = {  # "x,y,z": 4 lines
    "8,13,2": ["Castle", "LEVEL 7", "Ride the white", "Electra Cores!"],
    "10,13,20": ["Grab the", "Grappling Hook", "and pull to", "the targets!"],
    "6,13,37": ["LEVEL 8", "", "coming soon!", ""],
}
data = {
    "name": "Castle Level 7",
    "size": [W, H, L],
    "floor": FLOOR,
    "start": [6, FLOOR + 1, 3],
    "key": KEY,
    "signs": SIGNS,
    "layers": [["".join(row) for row in layer] for layer in grid],
}
out = "src/main/resources/data/horrormod/castle/level_seven.json"
os.makedirs(os.path.dirname(out), exist_ok=True)
with open(out, "w") as f:
    json.dump(data, f, indent=1)
print("wrote", out)
