# Game-Engine-Genetic-Algorythm
Java game engine using LWJGL and OpenGL. Genetic algorythm that tries to find a path to finish.

Further plans:
* [ ] Add classes Car, Obstacle
  * [x] Car should have an ability to move and turn with its own physics
  * [x] Function that checks intersection of two objects
    * [x] Broad phase
    * [x] Narrow phase 
  * [x] Add Finish class
* [ ] Make a road for a car 
* [ ] Rays from the center of a car
  * [ ] Function that returns distance to an object
* [ ] Add a neural network to the Car class
  * [ ] Takes three inputs: distance to objects from three rays
  * [ ] Returns one of 4 outputs: steer left, steer right, accelerate, stop
* [ ] Ability to lauch a generation
* [ ] Realise selection algorythm
* [ ] Add ability to build paths
* [ ] Ring road
