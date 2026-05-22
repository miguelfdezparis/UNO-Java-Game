# UNO Game – Java Project

A console-based version of the UNO card game built in Java as our first real OOP project. We chose UNO because the rules are simple enough to understand but the logic behind it — turns, card effects, direction changes, CPU strategy — gave us enough to properly think about class design. The game supports 2 to 4 players, any combination of human and CPU, and wins are saved to a CSV so the scoreboard persists between sessions.

**Authors:** Miguel Fernández de la Cigoña París & Xurxo Lago Álvarez  
DAW 1º — 2025/26

## Links

- GitHub: https://github.com/miguelfdezparis/uno-java-game
- Notion: https://climbing-girl-57c.notion.site/Java-Project-UNO-Game-34135a3d22f480248ba2dd12eec0131a

---

## How to run

1. Clone the repo and open it in **IntelliJ IDEA** as a Maven project.
2. Copy `.env.example` → `.env` (credentials are already filled in for localhost, nothing to change).
3. Set up the databases (see below).
4. Run `Main.java`.

No extra build steps needed — Maven pulls all dependencies automatically (`postgresql`, `mongodb-driver-sync`, `dotenv-java`).

---

## Project structure

```
src/main/java/org/example/
├── Main.java                   ← entry point, just starts the controller
├── controller/
│   └── GameController.java     ← all game flow: setup, turns, card effects
├── model/
│   ├── Card.java               ← abstract base class for all cards
│   ├── ColorCard.java          ← number cards (0–9), extends Card
│   ├── EffectCard.java         ← skip / reverse / +2, extends Card
│   ├── WildCard.java           ← wild / wild+4, extends Card
│   ├── Color.java              ← enum: RED, BLUE, GREEN, YELLOW, BLACK
│   ├── Value.java              ← enum: ZERO…NINE, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
│   ├── Deck.java               ← builds and shuffles the 108-card deck
│   ├── Hand.java               ← a player's cards
│   ├── Player.java             ← name, hand, and whether it's a CPU
│   ├── GameState.java          ← everything that changes during a game (turn order, discard pile, etc.)
│   └── Carta.java              ← simple data class representing a unique card type for the DB
├── view/
│   └── ConsoleView.java        ← all output and input, ANSI colours included
├── db/
│   ├── BarajaDAO.java          ← interface with obtenerBaraja() and guardarBaraja()
│   ├── DatabaseConnection.java ← reads .env and opens a PostgreSQL connection
│   ├── PostgresBarajaDAO.java  ← DAO implementation for PostgreSQL
│   └── MongoBarajaDAO.java     ← DAO implementation for MongoDB
└── utils/
    └── FileManager.java        ← loads and saves scores.csv
sql/
└── schema.sql                  ← creates the cartas table in PostgreSQL
```

We follow MVC: the controller never prints anything directly, the view never touches game logic, and the model knows nothing about either.

---

## Databases

The project uses two databases to store the 54 unique card types that make up the 108-card UNO deck. Both are implemented behind the same `BarajaDAO` interface — one class per database.

### PostgreSQL — via pgAdmin

Credentials are in `.env.example` (safe to share, they're localhost-only):

```
POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
POSTGRES_USER=postgres
POSTGRES_PASS=abc123
```

Setup steps:

1. Make sure PostgreSQL is running on `localhost:5432`.
2. Create the database in pgAdmin (or psql): `CREATE DATABASE unojavagame;`
3. Run the schema: `psql -U postgres -d unojavagame -f sql/schema.sql`

That creates the `cartas` table with three ENUM types (`tipo_carta`, `valor_carta`, `color_carta`) matching the Java class names and enums exactly.

### MongoDB — via MongoDB Compass

```
MONGO_URI=mongodb://localhost:27017
MONGO_DB=unojavagame
```

No setup needed. The `MongoBarajaDAO` creates the database and `cartas` collection automatically the first time it inserts data. Just connect Compass to `mongodb://localhost:27017` and you'll see it appear after the first run.

The `DatabaseConnection` class reads both sets of credentials from `.env` using `dotenv-java`, so credentials are never hardcoded.

---

## How the game works

```
Main → GameController.start()
         ↓
       showMenu()  →  1. Play  2. Rules  3. Scores  4. Exit
         ↓ (Play)
       setupPlayers()   asks how many players and whether each is human or CPU
         ↓
       GameState(players)  builds the deck, shuffles, deals 7 cards each
         ↓
       gameLoop()
         ├── humanTurn()    shows hand, player picks a card index or draws
         └── computerTurn() picks the best playable card (prefers action cards)
                            then applyEffect() handles SKIP / REVERSE / +2 / WILD / +4
         ↓ (someone empties their hand)
       showWinner()  →  updates allTimeScores  →  FileManager.saveScores()
```

The CPU picks whichever compatible card is an action card first; if nothing is playable it draws one and plays it immediately if it fits. Wild colour choice is decided by counting which colour appears most in the CPU's hand.

---

## OOP concepts used

| Concept | Where |
|---|---|
| Abstract class | `Card` — defines `hasEffect()` as abstract |
| Inheritance | `ColorCard`, `EffectCard`, `WildCard` extend `Card` |
| Polymorphism | `Deck` stores `ArrayList<Card>` with all three subtypes |
| Interface | `BarajaDAO` — two implementations (Postgres + Mongo) |
| Enums | `Color`, `Value` |
| MVC pattern | controller / model / view packages |
| File I/O | `FileManager` reads and writes `scores.csv` |
| HashMap | scoreboard in `GameController`, colour counting in CPU logic |

---

## Scores file

`scores.csv` is created automatically in the project root the first time someone wins. Format:

```
name,wins
Miguel,3
CPU,1
```

It persists between sessions — the scoreboard in the menu shows all-time wins.
