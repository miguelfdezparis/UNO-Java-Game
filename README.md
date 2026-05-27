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

- **Graphical interface** — Java Swing, dark green felt theme, card animations
- **2–4 players**, any mix of human and CPU
- **CPU AI** — prefers action cards, picks the best colour on wild cards
- **Card play animation** — when you play a card it slides from your hand to the discard pile with an ease-in-out curve
- **Background music** — plays automatically, loops, starts at second 10
- **Music toggle button** — 🔊/🔇 button in the top bar silences/restores music at any time
- **Sound effects** — different sounds for each card type (play, draw, skip, reverse, +2, +4, wild)
- **Text-to-speech** — voices in Spanish; when UNO is called: *"Queda una carta… UNOOO!"* (music ducks automatically); on win: *"[name] ha ganado!"*; amplified 4× for clarity
- **UNO penalty** — when you play down to 1 card a red `¡UNO! (3)` button appears with a 3-second countdown; miss it and you draw 2 penalty cards; CPU always calls UNO automatically
- **Live scoreboard** — all-time wins from `scores.csv` shown in the sidebar during the game, updated in real time after each match
- **Rules screen** — the setup dialog has a "Reglas" tab with the full UNO ruleset
- **Deck recycling** — when the draw pile runs out the discard pile (minus the top card) is automatically shuffled and reused; logged in the game history
- **Play again / Exit** — dialog at the end of each game
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
│       ├── SetupDialog.java       ← setup screen with Jugar / Reglas tabs
│       ├── CardView.java          ← individual card rendering (Java2D)
│       ├── ColorPickerDialog.java ← wild colour selector
│       ├── DrawnCardDialog.java   ← prompt to play a drawn card
│       ├── MusicPlayer.java       ← MP3 background music, mute toggle
│       └── SoundEffect.java       ← procedural sound effects + amplified TTS
├── db/
│   ├── BarajaDAO.java             ← interface
│   ├── DatabaseConnection.java    ← reads .env
│   ├── PostgresBarajaDAO.java
│   └── MongoBarajaDAO.java
└── utils/
    ├── FileManager.java           ← loads and saves scores.csv
    ├── BarajaCatalog.java
    └── CartaConverter.java
src/main/resources/
└── music.mp3                      ← background music
sql/
└── schema.sql                     ← PostgreSQL schema
iniciar-bases-datos.bat            ← one-click database setup (Windows)
```

---

## How the game works

```
Main → SetupDialog (DB choice, players, rules tab)
         ↓
       GUIGameController.start()
         ↓
       gameLoop()  [background thread]
         ├── doComputerTurn()    picks best card, draws if nothing fits
         │                       auto-calls UNO when hand reaches 1 card
         └── doHumanTurn()       player clicks a card → slide animation
                                 → applyEffect() (SKIP / REVERSE / +2 / WILD / +4)
                                 → checkUnoAfterPlay() if hand = 1 card
                                     shows ¡UNO! countdown button (3 s)
                                     miss it → draw 2 penalty cards
         │
         │   safeDraw() wraps every draw:
         │   deck empty → GameState recycles discard pile automatically
         ↓
       (someone empties their hand)
       SoundEffect.win()  →  music ducks  →  TTS announces winner
       frame.updateScoreboard()  →  scores.csv updated
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
| Concurrency | game loop in background thread, `BlockingQueue` for human input, `CountDownLatch` for UNO challenge and dialogs |
| Observer-like | `Consumer<Integer>` callback from frame to controller |

---

## Scores

`scores.csv` is created automatically in the project root the first time someone wins:

```
name,wins
Miguel,3
CPU,1
```

Persists between sessions and is displayed live in the scoreboard panel on the right side of the game window.
