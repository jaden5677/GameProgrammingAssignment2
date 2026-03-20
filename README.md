# Game Programming Assignment 2

## What does this assinment entail?
### Personal Goal with this project
This assignment intends to make use of everything I've learnt so far in the course COMP3609 as well as other practical applications of Object Oriented Concepts

### Requirements
The main requirements of this project are the development of a "visual playground" where the player's sprit moves around a background that is several times larger then the GamePanel. As the player's sprite moves, oit encounters other game entities which behave in a certain way when a collision takes place.

The project must have:

* A large background several times larger than the game panel. It must be several times larger thnthe GamePanel. The playr's sprite should be able to smoothly scroll through the background in 4 directions using the arrow keys or WASD. The player's sprite should not be able to scroll beyond the edges of the backround in any direction

* Image effects: At least three impemented effects using Java2D image processing - GrayScale, Disappear/Fade, Tint

* Sprite Animation: Must be frame animated. AT LEAST one additional sprite should be animated, ideally using a sprite sheet(decorative entity, collectible, or environmental element.

* Solid Objects

* Double Buffering and threads

* Sound Clips

* Terminating the game

* Information Display




## How I use this assignment to develop my OOP skills

## Design Patterns
### Creational
* Abstract Factory for the different types of mutable game entities(enemies, npcs, etc)

* Cloning for different types of immutable game entities(trees, rocks, foliage, dungeons(maybe)

* Singleton for managing game objects, sounds and images

### Structural
* Bridge
* Decoration
* FLYWEIGHT FOR LARGE ENTITY MANAGEMENT

### Behavioral
* Command
* Memento
* States

Note that Hashing will be used for entity location mapping after generation 


