-- ─────────────────────────────────────────────────────
--  schema.sql  —  UNO Game
--  Ejecutar: psql -U postgres -d uno_game -f sql/schema.sql
-- ─────────────────────────────────────────────────────

DROP TYPE IF EXISTS tipo_carta   CASCADE;
DROP TYPE IF EXISTS valor_carta  CASCADE;
DROP TYPE IF EXISTS color_carta  CASCADE;
DROP TABLE IF EXISTS cartas      CASCADE;

-- tipo de carta: las tres subclases de Card
CREATE TYPE tipo_carta AS ENUM (
    'ColorCard',
    'EffectCard',
    'WildCard'
);

-- valor: nombres del enum Value.java
CREATE TYPE valor_carta AS ENUM (
    'ZERO',
    'ONE',
    'TWO',
    'THREE',
    'FOUR',
    'FIVE',
    'SIX',
    'SEVEN',
    'EIGHT',
    'NINE',
    'SKIP',
    'REVERSE',
    'DRAW_TWO',
    'WILD',
    'WILD_DRAW_FOUR'
);

-- color: nombres del enum Color.java
CREATE TYPE color_carta AS ENUM (
    'RED',
    'BLUE',
    'GREEN',
    'YELLOW',
    'BLACK'
);

-- un registro por combinacion unica (tipo, valor, color)
-- 54 filas en total → 108 cartas sumando apariciones
CREATE TABLE IF NOT EXISTS cartas (
    id          SERIAL       PRIMARY KEY,
    tipo        tipo_carta   NOT NULL,
    valor       valor_carta  NOT NULL,
    color       color_carta  NOT NULL,
    apariciones INT          NOT NULL CHECK (apariciones > 0),
    UNIQUE (tipo, valor, color)
);
