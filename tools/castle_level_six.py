"""Castle LEVEL SIX, designed by Sam's daughter. The whole course is DATA: a 3D grid of letters
(one picture per height layer), written to src/main/resources/data/horrormod/castle/level_six.json.
The game reads that file and builds the course in front of you when you use the "Castle Level 6" item.

Local directions: x = to your right (0..W-1), z = forward away from you (0..L-1), y = up (0..H-1).
usage: python3 tools/castle_level_six.py
"""
import json, os

W, L, H = 13, 50, 21   # 0.7.1: one taller (a roof at y=20 that is also Level 7's floor)
# What each letter means (the game turns these into real blocks)
KEY = {
    " ": "air", "#": "stone bricks", "d": "deepslate bricks", "t": "deepslate tiles", "c": "cracked stone bricks",
    "m": "mossy stone bricks", "w": "water (safe soft landing)", "L": "lantern", "l": "soul lantern",
    "^": "stone brick stairs (climbing ledge)", "u": "stone brick top slab (climbing ledge)", "v": "climbable vine",
    "H": "ladder (climb back out of the pool)", "p": "Stray Blast Pad", "=": "zip line rope (chain)",
    "C": "chest with the Zip Line Hook", "K": "chest with the Level 7 Key", "g": "Level 7 Gate (locked)",
    "b": "polished blackstone", "G": "gold block", "o": "sign", "S": "a Stray stands here",
    "U": "Go Up Pad (step on it: whoosh up to Level 7's start, upstairs)",
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

# foundation
box(0, W - 1, 0, 0, 0, L - 1, "t")
# castle walls: left, right, back (with door), end wall (with gate), level 7 end
for z in range(L):
    for y in range(1, 19):
        put(0, y, z, wall_ch(y, z)); put(W - 1, y, z, wall_ch(y, z + 3))
for x in range(W):
    for y in range(1, 19):
        put(x, y, 0, wall_ch(y, x)); put(x, y, 44, wall_ch(y, x + 1)); put(x, y, L - 1, wall_ch(y, x + 2))
box(5, 7, 1, 3, 0, 0, " ")                       # front door
# 0.7.1: the walls go one higher and a solid ROOF closes Level 6 (Level 7 is built on top of it)
for z in range(L):
    put(0, 19, z, "d"); put(W - 1, 19, z, "d")
for x in range(W):
    put(x, 19, 0, "d"); put(x, 19, 44, "d"); put(x, 19, L - 1, "d")
box(0, W - 1, 20, 20, 0, L - 1, "t")
# spooky lanterns on little wall shelves
for z in range(4, 44, 6):
    for x, sx in ((1, 1), (W - 2, W - 2)):
        put(sx, 10, z, "d"); put(sx, 11, z, "l" if z % 12 == 4 else "L")
put(4, 1, 1, "L"); put(8, 1, 1, "L")

# safe water pool under the whole course (falling = splash, never hurt)
box(1, W - 2, 1, 1, 9, 43, "w")

# (1) ROCK CLIMBING: a tall wall with stair and slab ledges zig-zagging up it
box(1, W - 2, 1, 8, 8, 8, "#")
for i, y in enumerate(range(1, 8)):
    put(2 + i, y, 7, "^" if i % 2 == 0 else "u")
for y in range(1, 9):                            # ladder out of the pool
    put(1, y, 9, "H")
put(4, 1, 2, "o")                                 # sign: Level 6

# (2) VINE PARKOUR: climb the vine wall, then jump across mossy blocks (vines catch you in the gaps)
box(4, 8, 9, 13, 10, 10, "m")
for x in range(4, 9):
    for y in range(9, 14):
        put(x, y, 9, "v")
put(6, 13, 12, "m"); put(6, 13, 11, "v"); put(6, 12, 11, "v")
put(5, 13, 14, "m"); put(5, 13, 13, "v"); put(5, 12, 13, "v")
put(6, 13, 15, "v"); put(6, 12, 15, "v")

# (3) STRAYS: ledges on the walls, and the Blast Pad in front of the big gap
box(3, 9, 13, 13, 16, 18, "#")
put(3, 14, 16, "l"); put(9, 14, 16, "L")
put(6, 13, 18, "p")
put(7, 14, 17, "o")                               # sign: stand on the pad
for z in (13, 20):
    put(1, 15, z, "d"); put(1, 16, z, "S")
    put(W - 2, 15, z, "d"); put(W - 2, 16, z, "S")
# landing platform across the gap (z 19..23 is the gap)
box(3, 9, 13, 13, 24, 27, "#")
put(4, 14, 25, "C"); put(8, 14, 25, "o")          # hook chest + sign
put(3, 14, 24, "L"); put(9, 14, 24, "l")

# (4) ZIP LINE rope (chains) from the landing platform to the final platform
for z in range(27, 38):
    put(6, 16, z, "=")

# (5) FINAL PLATFORM with the key chest and the locked gate into level 7
box(3, 9, 13, 13, 37, 43, "#")
put(6, 13, 41, "G"); put(6, 14, 41, "K")
put(8, 14, 39, "o")
put(4, 14, 43, "L"); put(8, 14, 43, "L")
box(5, 7, 14, 16, 44, 44, "g")
# the LEVEL 7 room: a big gold 7 on the floor and the Go Up Pads that whoosh you upstairs into Level 7
box(1, W - 2, 13, 13, 45, 48, "b")
SEVEN = ["GGGGG", "...G.", "..G..", ".G..."]
for r, row in enumerate(SEVEN):
    for c, ch in enumerate(row):
        if ch == "G":
            put(4 + c, 13, 45 + r, "G")
put(6, 14, 46, "o")
box(9, 10, 13, 13, 46, 47, "U")                    # Go Up Pads (2x2, green arrow)

SIGNS = {  # "x,y,z": 4 lines
    "4,1,2": ["Castle", "LEVEL 6", "Climb the", "ledges up!"],
    "7,14,17": ["Stand on the", "Blast Pad!", "Strays will", "blast you over"],
    "8,14,25": ["Take the", "Zip Line Hook", "right-click by", "the chain!"],
    "8,14,39": ["Get the key,", "use it on", "the gate", "to go on!"],
    "6,14,46": ["Go UP to", "LEVEL 7!", "Step on the", "green arrow pad"],
}
data = {
    "name": "Castle Level 6",
    "size": [W, H, L],
    "key": KEY,
    "signs": SIGNS,
    "pad_jump": 7,
    # 0.7.1 (June): Level 7 is stacked right ON TOP of Level 6, like one tall castle tower.
    # Level 7's bottom sits at y=21, just above Level 6's roof (y=20), so its lava and void pit
    # rest on that roof and falling never drops you into Level 6 (you're whooshed back to the start pad).
    "stack": {
        "level_seven_y": 21,
        # boxes in LEVEL 7's own spots (x0,x1,y0,y1,z0,z1) bricked up when stacked: its front door
        # would open onto thin air high up, so it's closed (you come in by the Go Up Pad instead)
        "seal": [[5, 7, 13, 15, 0, 0]],
    },   # how far forward the Blast Pad throws you (to the landing platform)
    "layers": [["".join(row) for row in layer] for layer in grid],
}
out = "src/main/resources/data/horrormod/castle/level_six.json"
os.makedirs(os.path.dirname(out), exist_ok=True)
with open(out, "w") as f:
    json.dump(data, f, indent=1)
print("wrote", out)
