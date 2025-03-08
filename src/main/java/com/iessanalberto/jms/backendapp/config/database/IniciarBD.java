package com.iessanalberto.jms.backendapp.config.database;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class IniciarBD {

    @Value("${spring.datasource.url.sinbd}")
    private String bdUrlSinBd;

    @Value("${spring.datasource.nombre}")
    private String bdName;

    @Value("${spring.datasource.username}")
    private String bdUser;

    @Value("${spring.datasource.password}")
    private String bdPassword;

    @PostConstruct
    public void init() {
        try {
            crearBaseDeDatosSiNoExiste();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }


    private void crearBaseDeDatosSiNoExiste() throws SQLException {
        try (Connection connection = DriverManager.getConnection(bdUrlSinBd, bdUser, bdPassword);
             PreparedStatement checkDbStmt = connection.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {

            checkDbStmt.setString(1, bdName);
            try (ResultSet rs = checkDbStmt.executeQuery()) {
                if (!rs.next()) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE " + bdName);
                        System.out.println("Base de datos " + bdName + " creada correctamente.");
                    }
                }
            }
        }
    }
}

