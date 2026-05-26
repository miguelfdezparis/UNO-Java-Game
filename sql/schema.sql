DROP TABLE IF EXISTS cartas;

CREATE TABLE IF NOT EXISTS cartas (
    id          SERIAL       PRIMARY KEY,
    tipo        VARCHAR(20)  NOT NULL,
    valor       VARCHAR(20)  NOT NULL,
    color       VARCHAR(20)  NOT NULL,
    apariciones INT          NOT NULL,
    UNIQUE (tipo, valor, color)
);

GRANT ALL ON TABLE cartas TO PUBLIC;
GRANT USAGE, SELECT ON SEQUENCE cartas_id_seq TO PUBLIC;
