package org.example.db;

import org.example.model.Carta;
import java.sql.*;
import java.util.ArrayList;

public class PostgresBarajaDAO implements BarajaDAO {

    @Override
    public void inicializar(ArrayList<Carta> catalogo) {
        String insert = "INSERT INTO cartas (tipo, valor, color, apariciones) VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (tipo, valor, color) DO UPDATE SET apariciones = EXCLUDED.apariciones";
        try (Connection con = DatabaseConnection.conectar();
             Statement st = con.createStatement();
             PreparedStatement ps = con.prepareStatement(insert)) {

            st.executeUpdate("DELETE FROM cartas");

            for (Carta c : catalogo) {
                ps.setString(1, c.getTipo());
                ps.setString(2, c.getValor());
                ps.setString(3, c.getColor());
                ps.setInt(4, c.getApariciones());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error al inicializar PostgreSQL: " + e.getMessage());
        }
    }

    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        String sql = "INSERT INTO cartas (tipo, valor, color, apariciones) VALUES (?, ?, ?, ?) ON CONFLICT (tipo, valor, color) DO UPDATE SET apariciones = EXCLUDED.apariciones";
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

    @Override
    public void actualizarApariciones(String valor, String color, int nuevas) {
        String sql = "UPDATE cartas SET apariciones = ? WHERE valor = ? AND color = ?";
        try (Connection con = DatabaseConnection.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevas);
            ps.setString(2, valor);
            ps.setString(3, color);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar en PostgreSQL: " + e.getMessage());
        }
    }

    @Override
    public void eliminarCarta(String valor, String color) {
        String sql = "DELETE FROM cartas WHERE valor = ? AND color = ?";
        try (Connection con = DatabaseConnection.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setString(2, color);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar en PostgreSQL: " + e.getMessage());
        }
    }
}
