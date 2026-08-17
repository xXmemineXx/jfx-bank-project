package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Loans;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoansDAO
{
	//get all loans
    public List<Loans> getAllLoans() 
    {
        List<Loans> LoansList = new ArrayList<>();
        String sql = "SELECT * FROM loans";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert Loans object to the returned list
            while (rs.next()) {
                Loans loan = new Loans(
                    rs.getInt("amount"),
                    rs.getString("loan_id"),
                    rs.getString("debtor_id"),
                    rs.getTimestamp("phone").toLocalDateTime()
                );
                LoansList.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LoansList;
    }
}