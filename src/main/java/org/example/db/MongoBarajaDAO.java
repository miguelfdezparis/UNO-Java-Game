package org.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.example.model.Carta;

import java.util.ArrayList;
import java.util.List;

public class MongoBarajaDAO implements BarajaDAO {

    @Override
    public void inicializar(ArrayList<Carta> catalogo) {
        try (MongoClient client = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> col = client.getDatabase("unojavagame").getCollection("Cartas");
            col.drop();
            List<Document> docs = new ArrayList<>();
            for (Carta c : catalogo) {
                docs.add(new Document()
                        .append("tipo", c.getTipo())
                        .append("valor", c.getValor())
                        .append("color", c.getColor())
                        .append("apariciones", c.getApariciones()));
            }
            col.insertMany(docs);
        } catch (Exception e) {
            System.err.println("Error al inicializar MongoDB: " + e.getMessage());
        }
    }

    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        try (MongoClient client = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> col = client.getDatabase("unojavagame").getCollection("Cartas");
            col.drop();
            List<Document> docs = new ArrayList<>();
            for (Carta c : cartas) {
                docs.add(new Document()
                        .append("tipo", c.getTipo())
                        .append("valor", c.getValor())
                        .append("color", c.getColor())
                        .append("apariciones", c.getApariciones()));
            }
            col.insertMany(docs);
        } catch (Exception e) {
            System.err.println("Error al guardar en MongoDB: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Carta> obtenerBaraja() {
        ArrayList<Carta> lista = new ArrayList<>();
        try (MongoClient client = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> col = client.getDatabase("unojavagame").getCollection("Cartas");
            for (Document doc : col.find()) {
                lista.add(new Carta(
                        doc.getString("tipo"),
                        doc.getString("valor"),
                        doc.getString("color"),
                        doc.getInteger("apariciones")));
            }
        } catch (Exception e) {
            System.err.println("Error al leer de MongoDB: " + e.getMessage());
        }
        return lista;
    }
}
