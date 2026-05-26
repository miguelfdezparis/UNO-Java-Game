# UNO Game – Java Project

A graphical UNO card game built in Java as our first real OOP project. We chose UNO because the rules are simple enough to understand but the logic behind it — turns, card effects, direction changes, CPU strategy — gave us enough to properly think about class design. The game supports 2 to 4 players, any combination of human and CPU, with a full GUI, background music, sound effects, and text-to-speech.

**Authors:** Miguel Fernández de la Cigoña París & Xurxo Lago Álvarez  
DAW 1º — 2025/26

## Links

- GitHub: https://github.com/miguelfdezparis/UNO-Java-Game
- Notion: https://climbing-girl-57c.notion.site/Java-Project-UNO-Game-34135a3d22f480248ba2dd12eec0131a

---

## How to run

1. Clone the repo and open it in **IntelliJ IDEA** as a Maven project.
2. Run `iniciar-bases-datos.bat` — it sets up PostgreSQL, starts MongoDB, and generates the `.env` file automatically.
3. Run `Main.java`.

No extra build steps needed — Maven pulls all dependencies automatically.

> **Console mode:** run `Main.java` with the `--console` argument to skip the GUI.

---

## First-time setup (databases)

Run `iniciar-bases-datos.bat` from the project root. It will:

- Ask for your PostgreSQL password (the one you use in pgAdmin)
- Create the `unojavagame` database and schema
- Start MongoDB
- Write the `.env` file with all credentials

The `.env` must be in the same folder as the JAR / project root:

```
POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
POSTGRES_USER=postgres
POSTGRES_PASS=yourpassword
MONGO_URI=mongodb://localhost:27017/unojavagame
MONGO_DB=unojavagame
```

---

## Features

- **Graphical interface** built with Java Swing — dark theme, animated card hand
- **2–4 players**, any mix of human and CPU
- **CPU AI** — prefers action cards, picks the best colour on wild cards
- **Background music** — plays automatically, loops, starts at second 10
- **Sound effects** — different sounds for each card type (play, draw, skip, reverse, +2, +4, wild)
- **Text-to-speech** — when someone reaches UNO: *"Queda una carta… UNOOO!"* (music ducks automatically); when someone wins: *"[name] ha ganado!"*
- **Play again / Exit** — dialog at the end of each game
- **Persistent scoreboard** — wins saved to `scores.csv` between sessions
- **Database selector** — choose PostgreSQL, MongoDB, or no DB at the setup screen

---

## Project structure

```
src/main/java/org/example/
├── Main.java
├── controller/
│   └── GameController.java        ← console game flow
├── model/
│   ├── Card.java                  ← abstract base class
│   ├── ColorCard.java             ← number cards (0–9)
│   ├── EffectCard.java            ← skip / reverse / +2
│   ├── WildCard.java              ← wild / wild+4
│   ├── Color.java                 ← enum: RED, BLUE, GREEN, YELLOW, BLACK
│   ├── Value.java                 ← enum: ZERO…NINE, SKIP, REVERSE…
│   ├── Deck.java                  ← builds and shuffles the 108-card deck
│   ├── Hand.java                  ← a player's cards
│   ├── Player.java                ← name, hand, human/CPU flag
│   ├── GameState.java             ← turn order, discard pile, current colour
│   └── Carta.java                 ← data class for DB storage
├── view/
│   ├── ConsoleView.java           ← ANSI console output and input
│   └── gui/
│       ├── GUIGameController.java ← GUI game loop (background thread)
│       ├── UnoGameFrame.java      ← main game window
│       ├── SetupDialog.java       ← setup screen (DB, players)
│       ├── CardView.java          ← individual card rendering
│       ├── ColorPickerDialog.java ← wild colour selector
│       ├── MusicPlayer.java       ← MP3 background music with volume control
│       └── SoundEffect.java       ← procedural sound effects + TTS
├── db/
│   ├── BarajaDAO.java             ← interface
│   ├── DatabaseConnection.java    ← reads .env
│   ├── PostgresBarajaDAO.java
│   └── MongoBarajaDAO.java
└── utils/
    └── FileManager.java           ← loads and saves scores.csv
src/main/resources/
└── music.mp3                      ← background music
sql/
└── schema.sql                     ← PostgreSQL schema
iniciar-bases-datos.bat            ← one-click database setup (Windows)
```

---

## How the game works

```
Main → SetupDialog (DB choice, players)
         ↓
       GUIGameController.start()
         ↓
       gameLoop()  [background thread]
         ├── doComputerTurn()   picks best card, draws if nothing fits
         └── doHumanTurn()      player clicks a card or the draw button
                                applyEffect() handles SKIP / REVERSE / +2 / WILD / +4
         ↓ (someone empties their hand)
       SoundEffect.win()  →  music ducks  →  TTS announces winner
       showWinnerOverlay()  →  Play again / Exit
```

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
| HashMap | scoreboard, CPU colour counting |
| Concurrency | game loop in background thread, `BlockingQueue` for human input |

---

## Scores

`scores.csv` is created automatically in the project root the first time someone wins:

```
name,wins
Miguel,3
CPU,1
```

Persists between sessions — visible in the end-of-game screen.
