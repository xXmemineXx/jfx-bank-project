package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Returns;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReturnsDAO
{
	//get all returns
    public List<Returns> getAllReturns() 
    {
        List<Returns> returnsList = new ArrayList<>();
        String sql = "SELECT * FROM returned";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert Returns object to the returned list
            while (rs.next()) {
                Returns returned = new Returns(
                    rs.getBoolean("fully_returned"),
                    rs.getInt("returned_amount"),
                    rs.getInt("unpayed"),
                    rs.getString("return_id"),
                    rs.getString("loan_id"),
                    rs.getTimestamp("return_date").toLocalDateTime()
                );
                returnsList.add(returned);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnsList;
    }
}