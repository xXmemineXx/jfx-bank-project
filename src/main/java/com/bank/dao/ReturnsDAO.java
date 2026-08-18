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
        String sql = "SELECT " +
                        "r.loan_id, " +
                        "r.return_id, " +
                        "c.first_name, " +
                        "r.return_date, " +
                        "r.returned_amount, " +
                        "l.amount as loan_amount, " +
                        "case " +
                          "WHEN fully_returned = TRUe then 'fully returned' " +
                          "else 'still in debt' " +
                        "end as status, " +
                        "r.fully_returned, " +
                        "r.unpayed " +
                    "FROM returned r " +
                      "JOIN loans l ON l.loan_id = r.loan_id " +
                      "JOIN clients c ON l.debtor_id = c.account_id";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert Returns object to the returned list
            while (rs.next()) {
                Returns returned = new Returns(
                    rs.getString("status"),
                    rs.getInt("loan_amount"),
                    rs.getInt("returned_amount"),
                    rs.getInt("unpayed"),
                    rs.getString("return_id"),
                    rs.getString("loan_id"),
                    rs.getString("first_name"),
                    rs.getTimestamp("return_date").toLocalDateTime(),
                    rs.getBoolean("fully_returned")
                );
                returnsList.add(returned);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnsList;
    }
}