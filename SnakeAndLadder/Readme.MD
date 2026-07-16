# Snake and Ladder - Low Level Design

## Overview

This project is an object-oriented implementation of the classic Snake and Ladder game.

The primary objective of this project is **not** to build a production-ready game, but to practice **Low Level Design (LLD)** principles such as:

- Responsibility assignment
- Encapsulation
- Abstraction
- Extensibility
- SOLID principles
- Clean API design

---

# Requirements

The game supports the following features:

- Configurable board size.
- Multiple players.
- Standard six-faced dice.
- Multiple snakes and ladders.
- Turn-based gameplay.
- A player reaching the last cell wins.
- If a dice roll overshoots the board, the player remains in the same position.
- At most one transition (snake/ladder) is applied per move.

---

# Assumptions

To keep the implementation focused on design, the following assumptions were made:

- Only one dice is used.
- The dice has six faces.
- A player cannot move beyond the last cell.
- Transition chaining is not supported.
  - Example:
    - 5 -> 20 (ladder)
    - 20 -> 8 (snake)
    - Landing on 5 ends at 20.
- Invalid board configurations are not validated.
- This implementation focuses on the core game logic and does not include any UI.

---

# Design Decisions

## 1. Why there is no Cell class?

Initially, a Cell abstraction was considered.

```text
Cell
--------
cellNumber
transition
```

However, most cells are empty and contain no behavior.

The board only needs to answer:

> "If someone lands on this position, where should they end up?"

Therefore introducing a Cell object would add unnecessary abstraction.

Instead, the board maintains

```java
Map<Integer, Transition>
```

which provides efficient lookup and keeps the design simple.

---

## 2. Why Transition instead of separate Snake and Ladder classes?

Both Snake and Ladder represent the same concept:

> Landing on one position moves the player to another position.

Instead of duplicating logic,

```text
Snake

Ladder
```

the implementation introduces

```text
Transition
```

Benefits:

- Removes duplicate logic.
- Easier to extend.
- Follows Open/Closed Principle.

Future transitions like

- Portal
- Trap
- Teleporter

can be added without modifying the Game class.

---

## 3. Why does Board own transitions?

The Game should orchestrate the gameplay, but it should not know how transitions are stored.

Instead of

```java
Transition t = board.getTransition(position);
```

the Board exposes

```java
resolvePosition(position)
```

This hides implementation details and improves encapsulation.

---

## 4. Why does Game orchestrate everything?

The Game class owns the rules of Snake and Ladder.

Responsibilities include:

- Managing turns.
- Rolling the dice.
- Applying transitions.
- Determining the winner.
- Driving the game loop.

The Game acts as the orchestrator while delegating specialized work to other classes.

---

## 5. Why Player only stores state?

A Player does not make strategic decisions in Snake and Ladder.

Unlike games such as Chess or Poker, the player cannot decide:

- where to move
- whether to climb a ladder
- whether to avoid a snake

Therefore Player only models game state.

```text
Player
-------
name
position
```

The Game owns the gameplay logic.

---

# Class Responsibilities

## Game

Responsible for:

- Running the game loop
- Managing turns
- Rolling dice
- Updating player positions
- Declaring winner

---

## Board

Responsible for:

- Board size
- Transition lookup
- Resolving final position

---

## Player

Responsible for:

- Player identity
- Current position

---

## Dice

Responsible for:

- Generating random dice values

---

## Transition

Represents movement from one position to another.

Different transition implementations can be added without modifying the Game.

---

# High Level Class Diagram

```text
                    +----------------+
                    |     Game       |
                    +----------------+
                    | Board          |
                    | List<Player>   |
                    | Dice           |
                    +----------------+
                           |
          +----------------+----------------+
          |                                 |
          |                                 |
+------------------+               +----------------+
|      Board       |               |      Dice      |
+------------------+               +----------------+
| size             |               | roll()         |
| transitions      |               +----------------+
+------------------+
          |
          |
          v
+------------------+
|   Transition     |
+------------------+
| apply()          |
+------------------+
          ^
          |
+---------------------------+
| SnakeLadderTransition     |
+---------------------------+

+------------------+
|      Player      |
+------------------+
| name             |
| position         |
+------------------+
```

---

# SOLID Principles Applied

### Single Responsibility Principle

Every class has one primary responsibility.

- Game -> Game orchestration
- Dice -> Dice rolling
- Board -> Transition resolution
- Player -> Player state

---

### Open Closed Principle

The Transition abstraction allows new transition types to be introduced without modifying Game.

---

### Dependency Inversion Principle

The Game depends on the Dice abstraction instead of a concrete implementation.

Future implementations:

- LoadedDice
- MultipleDice
- MockDice

can be added easily.

---

# Possible Extensions

This design can be extended to support:

- Multiple dice
- Different board sizes
- Portals
- Traps
- Crocodiles
- Power-ups
- Custom winning conditions
- AI players
- Online multiplayer
- Board validation
- Event listeners
- Game replay

without major modifications to the existing architecture.

---

# Future Improvements

Some improvements intentionally left out to keep the design simple:

- Board validation
- Builder pattern for game creation
- Game configuration object
- GameParticipant abstraction for larger gaming platforms
- Event-driven architecture
- Observer pattern for UI updates
- Strategy pattern for winning rules

---

# Learning Outcomes

This exercise helped practice:

- Identifying entities
- Assigning responsibilities
- Eliminating unnecessary abstractions
- Designing for extensibility
- Encapsulation
- Thinking beyond implementation towards maintainable design