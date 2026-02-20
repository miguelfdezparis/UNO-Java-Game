```mermaid
    classDiagram
    direction LR

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

    %% Relaciones
    Carta --> Color
    Carta --> Valor

    Baraja "1" *-- "many" Carta
    Mano "1" *-- "many" Carta
    PilaDescarte "1" *-- "many" Carta

    Jugador "1" *-- "1" Mano
    JuegoUNO "1" *-- "many" Jugador
    JuegoUNO "1" *-- "1" Baraja
    JuegoUNO "1" *-- "1" PilaDescarte

