package org.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.example.model.Carta;

import java.util.ArrayList;

public class MongoBarajaDAO implements BarajaDAO {


    @Override
    public void guardarBaraja(ArrayList<Carta> cartas) {
        try (MongoClient client = DatabaseConnection.crearCliente()) {
            MongoCollection<Document> coleccionCartas = client.getDatabase("unojavagame").getCollection("Cartas");

            Document coleccionCartas =
        } catch (Exception e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Carta> obtenerBaraja() {
        return null;
    }
}

