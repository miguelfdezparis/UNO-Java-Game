package org.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.example.model.Carta;

import java.util.ArrayList;

public class MongoBarajaDAO implements BarajaDAO {


    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        try (MongoClient client = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> coleccionCartas = client.getDatabase("unojavagame").getCollection("Cartas");

            for (Carta c : cartas) {
                Document cartaDoc = new Document()
                        .append("tipo", c.getTipo())
                        .append("valor", c.getValor())
                        .append("color", c.getColor())
                        .append("apariciones", c.getApariciones());
                coleccionCartas.insertOne(cartaDoc);
            }
        } catch (Exception e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Carta> obtenerBaraja() {
        ArrayList<Carta> listaCartas = new ArrayList<>();
        try (MongoClient cli = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> coleccionCartas = cli.getDatabase("unojavagame").getCollection("Cartas");

            for (Document doc : coleccionCartas.find()) {
                listaCartas.add(new Carta(
                        doc.getString("tipo"),
                        doc.getString("valor"),
                        doc.getString("color"),
                        doc.getInteger("apariciones")));
            }
        } catch (Exception e) {
            System.out.println("Error al conectar a la base de datos:" + e.getMessage());
        }
        return listaCartas;
    }
}
