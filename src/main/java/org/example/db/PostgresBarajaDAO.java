package org.example.db;

import org.example.model.Carta;
import java.sql.*;
import java.util.ArrayList;

public class PostgresBarajaDAO implements BarajaDAO {

    @Override
    public void inicializar(ArrayList<Carta> catalogo) {
        try (Connection con = DatabaseConnection.conectar();
             Statement st = con.createStatement()) {

            // recrear schema limpio
            st.executeUpdate("DROP TABLE IF EXISTS cartas CASCADE");
            st.executeUpdate("DROP TYPE IF EXISTS tipo_carta CASCADE");
            st.executeUpdate("DROP TYPE IF EXISTS valor_carta CASCADE");
            st.executeUpdate("DROP TYPE IF EXISTS color_carta CASCADE");

            st.executeUpdate("CREATE TYPE tipo_carta  AS ENUM ('ColorCard','EffectCard','WildCard')");
            st.executeUpdate("CREATE TYPE valor_carta AS ENUM ('ZERO','ONE','TWO','THREE','FOUR','FIVE','SIX','SEVEN','EIGHT','NINE','SKIP','REVERSE','DRAW_TWO','WILD','WILD_DRAW_FOUR')");
            st.executeUpdate("CREATE TYPE color_carta AS ENUM ('RED','BLUE','GREEN','YELLOW','BLACK')");
            st.executeUpdate("CREATE TABLE cartas (id SERIAL PRIMARY KEY, tipo tipo_carta NOT NULL, valor valor_carta NOT NULL, color color_carta NOT NULL, apariciones INT NOT NULL, UNIQUE(tipo,valor,color))");

            // insertar catalogo completo
            String sql = "INSERT INTO cartas (tipo, valor, color, apariciones) VALUES (?::tipo_carta, ?::valor_carta, ?::color_carta, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Carta c : catalogo) {
                    ps.setString(1, c.getTipo());
                    ps.setString(2, c.getValor());
                    ps.setString(3, c.getColor());
                    ps.setInt(4, c.getApariciones());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

        } catch (SQLException e) {
            System.err.println("Error al inicializar PostgreSQL: " + e.getMessage());
        }
    }

    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        String sql = "INSERT INTO cartas (tipo, valor, color, apariciones) VALUES (?::tipo_carta, ?::valor_carta, ?::color_carta, ?) ON CONFLICT (tipo, valor, color) DO UPDATE SET apariciones = EXCLUDED.apariciones";
        try (Connection con = DatabaseConnection.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Carta c : cartas) {
                ps.setString(1, c.getTipo());
                ps.setString(2, c.getValor());
                ps.setString(3, c.getColor());
                ps.setInt(4, c.getApariciones());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error al guardar en PostgreSQL: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Carta> obtenerBaraja() {
        ArrayList<Carta> lista = new ArrayList<>();
        try (Connection con = DatabaseConnection.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT tipo, valor, color, apariciones FROM cartas")) {
            while (rs.next()) {
                lista.add(new Carta(rs.getString("tipo"), rs.getString("valor"), rs.getString("color"), rs.getInt("apariciones")));
            }
        } catch (SQLException e) {
            System.err.println("Error al leer de PostgreSQL: " + e.getMessage());
        }
        return lista;
    }
}
