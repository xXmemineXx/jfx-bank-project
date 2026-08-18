package com.bank.dao;

import com.bank.database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    /**
     * Inserts a plain text administrator row straight into PostgreSQL.
     */
    public boolean registerAdmin(String firstName, String lastName, String mail, String password) {
        String sql = "INSERT INTO admins (admin_first_name, admin_last_name, admin_mail, admin_password) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, mail);
            stmt.setString(4, password);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies plain text credentials by checking if a row matches both inputs.
     */
    public boolean authenticateAdmin(String email, String password) {
        String sql = "SELECT * FROM admins WHERE admin_mail = ? AND admin_password = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // If a matching row is found, access is granted instantly
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
