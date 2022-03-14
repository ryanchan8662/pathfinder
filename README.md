# pathfinder

Solves any traditional 2D maze drawn to standard. Provide a start and end point encoded by rgb(255, 0, 0) and rgb(0, 0, 255), respectively, and all lines encoded in black (rgb(0, 0, 0)).
Specify the input file during runtime, and the solution line will be printed as a copy of the file under "output.png". 

This program dynamically splices the image into a graph of cells, each occupying either a traversable (white) space or obstacle (black).
Using the A* algorithm, it will find an optimised path from the start to solution point. This is able to recognise untraversable paths and missing start/end points.

Notes and limitations:
- Optimised to work at 30+ iterations per second for small images (512x512)
- May hit stackoverflow error on large images where too many nodes must be stored.
- Elements or pixels not conforming 100% to colour specifications will be ignored.
- Traversal algorithm for initial search of red/blue landmark dots may sometimes fail.
- Final traversal algorithm can still be improved for even shorter pathing.
- Plans for the future to expand into image recognition and 3D robotic navigation.
