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

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static Connection conectar() throws SQLException {
        String url  = dotenv.get("POSTGRES_URL");
        String user = dotenv.get("POSTGRES_USER");
        String pass = dotenv.get("POSTGRES_PASS");
        if (url == null) throw new SQLException("No se encontro .env con credenciales PostgreSQL. Ejecuta iniciar-bases-datos.bat.");
        return DriverManager.getConnection(url, user, pass);
    }

    public static MongoClient crearCliente() {
        String uri = dotenv.get("MONGO_URI");
        if (uri == null) throw new RuntimeException("No se encontro .env con credenciales MongoDB. Ejecuta iniciar-bases-datos.bat.");
        return MongoClients.create(uri);
    }
}
