package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Admins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    
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

    //check if exists
    public Admins authenticateAdmin(String email, String password) {
        String sql = "SELECT * FROM admins WHERE admin_mail = ? AND admin_password = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Build and return the full Admin object directly from the database row fields
                    return new Admins(
                        rs.getInt("admin_id"),
                        rs.getString("admin_first_name"),
                        rs.getString("admin_last_name"),
                        rs.getString("admin_mail"),
                        rs.getString("admin_password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if authentication checks fail
    }
}
