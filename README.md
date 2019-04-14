# Game-Engine-Genetic-Algorythm
Java game engine using LWJGL and OpenGL. Genetic algorythm that tries to find a path to finish.

Further plans:
* [ ] Add classes Car, Obstacle
  * [x] Car should have an ability to move and turn with its own physics
  * [x] Function that checks intersection of two objects
    * [x] Broad phase
    * [x] Narrow phase 
  * [x] Add Finish class
    * [ ] Checkpoints and Finish
* [x] Make a road for a car 
* [x] Rays from the center of a car
  * [x] Function that returns distance to an object
* [x] Add a neural network to the Car class
  * [x] Takes three inputs: distance to objects from three rays
  * [x] Returns one of 2 outputs: steer left/right, accelerate/stop
* [x] Ability to lauch a generation
* [x] Realize selection algorythm
* [x] Add ability to build paths
* [ ] Ring road
