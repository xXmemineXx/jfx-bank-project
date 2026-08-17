package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Admins;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO
{
	//get allAdmins
    public List<Admins> getAdmins() 
    {
        List<Admins> adminList = new ArrayList<>();
        String sql = "SELECT * FROM admins";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert admin object to the returned list
            while (rs.next()) {
                Admins admins = new Admins(
                    rs.getInt("id"),
                    rs.getString("admin_last_name"),
                    rs.getString("admin_first_name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                adminList.add(admins);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminList;
    }

}