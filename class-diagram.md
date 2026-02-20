Copilot said: ```mermaid classDiagram direction LR %% ========================= %% MODEL (your UNO domain) %%
Mermaid
classDiagram
direction LR

%% =========================
%% MODEL (your UNO domain)
%% =========================
class Card {
  -Color color
  -Value value
  +Card(Color, Value)
  +getColor() Color
  +getValue() Value
  +isCompatible(Card, Color) boolean
}

class Deck {
  -List~Card~ cards
  +Deck()
  -createDeck()
  +shuffle()
  +draw() Card
  +size() int
}

class Hand {
  -List~Card~ cards
  +Hand()
  +addCard(Card)
  +playCard(int) Card
  +hasPlayableCard(Card, Color) boolean
  +isEmpty() boolean
  +show()
}

class DiscardPile {
  -Stack~Card~ cards
  +DiscardPile()
  +putCard(Card)
  +peekTop() Card
  +recycle() List~Card~
}

class Player {
  -String name
  -Hand hand
  +Player(String)
  +getName() String
  +getHand() Hand
}

class UnoGame {
  -List~Player~ players
  -Deck deck
  -DiscardPile discard
  -Card currentCard
  -Color currentColor
  -int currentTurn
  -int direction
  +UnoGame(List~Player~)
  +start()
  +playTurn()
  +applyEffect(Card)
  +advanceTurn()
  +getCurrentPlayer() Player
  +hasWinner() boolean
}

class Color {
  <<enumeration>>
  RED
  BLUE
  GREEN
  YELLOW
  BLACK
}

class Value {
  <<enumeration>>
  ZERO
  ONE
  TWO
  THREE
  FOUR
  FIVE
  SIX
  SEVEN
  EIGHT
  NINE
  SKIP
  REVERSE
  DRAW_TWO
  WILD
  WILD_DRAW_FOUR
}

%% =========================
%% VIEW (View)
%% =========================
class GameView {
  <<interface>>
  +showWelcome()
  +renderGameState(UnoGame)
  +renderPlayerHand(Player)
  +askTurnAction(UnoGame, Player) TurnAction
  +askCardIndexToPlay(Player) int
  +askColorChoice() Color
  +showInvalidMove(String)
  +showInfo(String)
  +showWinner(Player)
}

class ConsoleGameView {
  -Scanner in
  +ConsoleGameView()
  +showWelcome()
  +renderGameState(UnoGame)
  +renderPlayerHand(Player)
  +askTurnAction(UnoGame, Player) TurnAction
  +askCardIndexToPlay(Player) int
  +askColorChoice() Color
  +showInvalidMove(String)
  +showInfo(String)
  +showWinner(Player)
}

%% =========================
%% CONTROLLER (Controller)
%% =========================
class GameController {
  -UnoGame game
  -GameView view
  +GameController(UnoGame, GameView)
  +run()
  -handleTurn()
  -requestCardPlay(Player) TurnAction
  -resolveWildColorIfNeeded(Card) Color
}

class TurnAction {
  <<data>>
  +TurnActionType type
  +int cardIndex
  +Color chosenColor
}

class TurnActionType {
  <<enumeration>>
  PLAY_CARD
  DRAW_CARD
  PASS
}

%% =========================
%% RELATIONSHIPS (Domain)
%% =========================
Card --> Color
Card --> Value

Deck "1" *-- "many" Card
Hand "1" *-- "many" Card
DiscardPile "1" *-- "many" Card

Player "1" *-- "1" Hand
UnoGame "1" *-- "many" Player
UnoGame "1" *-- "1" Deck
UnoGame "1" *-- "1" DiscardPile

%% =========================
%% RELATIONSHIPS (MVC)
%% =========================
GameController --> UnoGame : controls
GameController --> GameView : uses
ConsoleGameView ..|> GameView

GameView --> TurnAction : returns
TurnAction --> TurnActionType
TurnAction --> Color : (optional)
