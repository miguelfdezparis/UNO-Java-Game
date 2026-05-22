package org.example.db;

import com.mongodb.MongoClientException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// lee las credenciales del .env para no tenerlas hardcodeadas en el codigo
public class DatabaseConnection {

    private static final Dotenv dotenv = Dotenv.load();

    public static Connection conectar() throws SQLException {
        String url  = dotenv.get("POSTGRES_URL");
        String user = dotenv.get("POSTGRES_USER");
        String pass = dotenv.get("POSTGRES_PASS");
        return DriverManager.getConnection(url, user, pass);
    }

    public static MongoClient crearCliente() {
        //Lee la uri (privada) del .env
        String uri  = dotenv.get("MONGO_URI");
        //Crea la base
        return MongoClients.create(uri);
    }
}
