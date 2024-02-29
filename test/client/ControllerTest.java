package client;

import javafx.application.Platform;
import server.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static server.AuthService.connection;


class ControllerTest {

    @org.junit.jupiter.api.Test
    void testPhoneValidation() {
        Controller controller = new Controller();
        String phone = controller.validate_phone("8-800-555-3535");
        assertEquals("+78005553535", phone);
    }

    @org.junit.jupiter.api.Test
    void connection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
            Statement stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}