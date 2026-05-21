package org.example.db;

import org.example.model.Carta;
import java.sql.*;
import java.util.ArrayList;

// dao de postgres, mete y saca los tipos de cartas de la tabla cartas
public class PostgresBarajaDAO implements BarajaDAO {

    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        String sql = "INSERT INTO cartas (tipo, valor, color, apariciones) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (Carta c : cartas) {
                ps.setString(1, c.getTipo());
                ps.setString(2, c.getValor());
                ps.setString(3, c.getColor());
                ps.setInt(4, c.getApariciones());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            // si postgres no esta corriendo peta aqui
            System.err.println("error al guardar en la bd: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Carta> obtenerBaraja() {
        ArrayList<Carta> lista = new ArrayList<>();
        String sql = "SELECT tipo, valor, color, apariciones FROM cartas";

        try (Connection con = DatabaseConnection.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Carta(
                    rs.getString("tipo"),
                    rs.getString("valor"),
                    rs.getString("color"),
                    rs.getInt("apariciones")
                ));
            }

        } catch (SQLException e) {
            System.err.println("error al leer de la bd: " + e.getMessage());
        }

        return lista;
    }
}
