"""Castle LEVEL NINE, designed by June (6): a "find the hidden buttons" PUZZLE room.
The whole room is DATA: a 3D grid of letters (one picture per height layer), written to
src/main/resources/data/horrormod/castle/level_nine.json. The "Castle Level 9" item builds it in front of you.
(Level 8 isn't designed yet, so Level 9 is NOT stacked on the tower.)

5 hidden buttons (press one: "Button 2 of 5 found!" and a lamp over the secret door lights up):
  1. behind a painting      (left wall, a little painting hangs right over the button: punch the painting to peek!)
  2. at the carpet's edge   (a red button peeking out next to the big red rug)
  3. in the bookshelf wall  (an oak button on one of the bookshelves on the right wall)
  4. up a small ladder      (on the wall of the little loft at the back left)
  5. behind a flower pot    (on the back wall, tucked behind the flower pot on the counter)
All 5 found -> the bookshelf-looking secret door in the back wall opens to a room with a key and a
"LEVEL 10 coming soon!" sign. June's extra: 3 skeletons guard the puzzle room (there's cover; never in the secret room). No cats.

Local directions: x = to your right (0..W-1), z = forward away from you (0..L-1), y = up (0..H-1). Floor is y=0.
usage: python3 tools/castle_level_nine.py
"""
import json, os

W, L, H = 15, 17, 9
KEY = {
    " ": "air", "#": "stone bricks", "d": "deepslate bricks", "m": "mossy stone bricks", "p": "oak planks",
    "k": "bookshelf", "r": "red carpet", "h": "hay bale (cover)", "L": "lantern", "H": "ladder (on the left wall)",
    "w": "spruce planks counter", "f": "flower pot with a poppy", "G": "gold block", "C": "chest with the Level 10 Key", "o": "sign",
    "s": "secret door (looks like a bookshelf, opens when all 5 buttons are found)",
    "1": "puzzle lamp 1", "2": "puzzle lamp 2", "3": "puzzle lamp 3", "4": "puzzle lamp 4", "5": "puzzle lamp 5",
    "E": "hidden button on a left-side wall (faces right)", "K": "hidden button on a right-side wall (faces left)",
    "N": "hidden button on the back wall (faces you)", "F": "hidden button on the floor",
}
grid = [[[" "] * W for _ in range(L)] for _ in range(H)]

def put(x, y, z, ch):
    grid[y][z][x] = ch

def box(x0, x1, y0, y1, z0, z1, ch):
    for y in range(y0, y1 + 1):
        for z in range(z0, z1 + 1):
            for x in range(x0, x1 + 1):
                put(x, y, z, ch)

DIV = 12  # the back wall with the secret door; the secret room is behind it (z 13..15)
for y in range(H):
    for z in range(L):
        for x in range(W):
            edge = x in (0, W - 1) or z in (0, DIV, L - 1) or y in (0, H - 1)
            if edge:
                put(x, y, z, "d" if y in (0, H - 1) else ("m" if (x * 3 + y * 5 + z) % 9 == 0 else "#"))
box(6, 8, 1, 3, 0, 0, " ")                      # front door
box(6, 8, 1, 3, DIV, DIV, "s")                  # secret door
for i in range(5):
    put(5 + i, 5, DIV, str(i + 1))              # progress lamps over the secret door
put(5, 1, 2, "o")                               # hint sign by the door

BUTTONS = []  # [x, y, z, where]
# 1. behind a painting (left wall); the painting entity hangs in the same block
put(1, 2, 4, "E"); BUTTONS.append([1, 2, 4, "behind the painting on the left wall"])
# 2. at the carpet's edge
box(5, 9, 1, 1, 4, 8, "r"); put(10, 1, 6, "F"); BUTTONS.append([10, 1, 6, "peeking out at the edge of the red rug"])
# 3. in the bookshelf wall (right wall)
box(13, 13, 1, 4, 3, 9, "k"); put(12, 2, 6, "K"); BUTTONS.append([12, 2, 6, "on a bookshelf in the bookshelf wall (right side)"])
# 4. up a small ladder to the loft (back left)
for y in range(1, 5):
    put(1, y, 10, "H")
box(1, 3, 4, 4, 11, 11, "p"); put(2, 5, 11, "N"); put(3, 5, 11, "L")
BUTTONS.append([2, 5, 11, "up the ladder, on the back wall of the little loft"])
# 5. behind a flower pot on the counter (back right)
box(10, 12, 1, 1, 10, 11, "w"); put(11, 2, 10, "f"); put(11, 2, 11, "N")
BUTTONS.append([11, 2, 11, "behind the flower pot on the counter"])
# cover from the skeletons
for x, z in ((3, 7), (10, 3), (8, 10)):
    put(x, 1, z, "h"); put(x, 2, z, "h")
put(1, 1, 1, "L"); put(13, 1, 1, "L")

# the SECRET ROOM: gold, a chest with the key, the LEVEL 10 sign
box(5, 9, 0, 0, 13, 15, "G")
put(7, 1, 15, "C"); put(6, 1, 14, "o")
put(1, 1, 13, "L"); put(13, 1, 13, "L")

SIGNS = {
    "5,1,2": ["Castle LEVEL 9", "Find the 5", "hidden buttons!", "Look everywhere!"],
    "6,1,14": ["LEVEL 10", "", "coming soon!", ""],
}
data = {
    "name": "Castle Level 9",
    "size": [W, H, L],
    "floor": 0,
    "start": [7, 1, 1],
    "key": KEY,
    "signs": SIGNS,
    "painting": [1, 2, 4],
    "buttons": BUTTONS,
    "skeletons": [[4, 1, 9], [11, 1, 5], [7, 1, 9]],
    "layers": [["".join(row) for row in layer] for layer in grid],
}
out = "src/main/resources/data/horrormod/castle/level_nine.json"
os.makedirs(os.path.dirname(out), exist_ok=True)
with open(out, "w") as f:
    json.dump(data, f, indent=1)
print("wrote", out)
