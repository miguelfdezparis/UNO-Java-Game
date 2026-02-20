```mermaid
classDiagram
direction LR

%% =========================
%% MODEL (tu dominio UNO)
%% =========================
class Carta {
  -Color color
  -Valor valor
  +Carta(Color, Valor)
  +getColor() Color
  +getValor() Valor
  +esCompatible(Carta, Color) boolean
}

class Baraja {
  -List~Carta~ cartas
  +Baraja()
  -crearBaraja()
  +barajar()
  +robar() Carta
  +size() int
}

class Mano {
  -List~Carta~ cartas
  +Mano()
  +agregarCarta(Carta)
  +jugarCarta(int) Carta
  +tieneCartaJugable(Carta, Color) boolean
  +estaVacia() boolean
  +mostrar()
}

class PilaDescarte {
  -Stack~Carta~ cartas
  +PilaDescarte()
  +ponerCarta(Carta)
  +verSuperior() Carta
  +reciclar() List~Carta~
}

class Jugador {
  -String nombre
  -Mano mano
  +Jugador(String)
  +getNombre() String
  +getMano() Mano
}

class JuegoUNO {
  -List~Jugador~ jugadores
  -Baraja baraja
  -PilaDescarte descarte
  -Carta cartaActual
  -Color colorActual
  -int turnoActual
  -int direccion
  +JuegoUNO(List~Jugador~)
  +iniciar()
  +jugarTurno()
  +aplicarEfecto(Carta)
  +avanzarTurno()
  +obtenerJugadorActual() Jugador
  +hayGanador() boolean
}

class Color {
  <<enumeration>>
  ROJO
  AZUL
  VERDE
  AMARILLO
  NEGRO
}

class Valor {
  <<enumeration>>
  CERO
  UNO
  DOS
  TRES
  CUATRO
  CINCO
  SEIS
  SIETE
  OCHO
  NUEVE
  SALTO
  REVERSA
  MAS_DOS
  CAMBIO_COLOR
  MAS_CUATRO
}

%% =========================
%% VIEW (Vista)
%% =========================
class GameView {
  <<interface>>
  +showWelcome()
  +renderGameState(JuegoUNO)
  +renderPlayerHand(Jugador)
  +askTurnAction(JuegoUNO, Jugador) TurnAction
  +askCardIndexToPlay(Jugador) int
  +askColorChoice() Color
  +showInvalidMove(String)
  +showInfo(String)
  +showWinner(Jugador)
}

class ConsoleGameView {
  -Scanner in
  +ConsoleGameView()
  +showWelcome()
  +renderGameState(JuegoUNO)
  +renderPlayerHand(Jugador)
  +askTurnAction(JuegoUNO, Jugador) TurnAction
  +askCardIndexToPlay(Jugador) int
  +askColorChoice() Color
  +showInvalidMove(String)
  +showInfo(String)
  +showWinner(Jugador)
}

%% =========================
%% CONTROLLER (Controlador)
%% =========================
class GameController {
  -JuegoUNO juego
  -GameView view
  +GameController(JuegoUNO, GameView)
  +run()
  -handleTurn()
  -requestCardPlay(Jugador) TurnAction
  -resolveWildColorIfNeeded(Carta) Color
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
%% RELACIONES (Dominio)
%% =========================
Carta --> Color
Carta --> Valor

Baraja "1" *-- "many" Carta
Mano "1" *-- "many" Carta
PilaDescarte "1" *-- "many" Carta

Jugador "1" *-- "1" Mano
JuegoUNO "1" *-- "many" Jugador
JuegoUNO "1" *-- "1" Baraja
JuegoUNO "1" *-- "1" PilaDescarte

%% =========================
%% RELACIONES (MVC)
%% =========================
GameController --> JuegoUNO : controla
GameController --> GameView : usa
ConsoleGameView ..|> GameView

GameView --> TurnAction : devuelve
TurnAction --> TurnActionType
TurnAction --> Color : (opcional)
