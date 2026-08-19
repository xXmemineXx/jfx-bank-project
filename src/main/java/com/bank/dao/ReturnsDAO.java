package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Returns;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReturnsDAO
{
    /**
     * Amount still owed on a loan after every recorded repayment is summed.
     * Always subtracts in Java so JDBC parameter binding cannot hide previous payments.
     */
    public int getOutstandingAmount(String loanId, int loanAmount) {
        String sql = "SELECT COALESCE(SUM(returned_amount), 0) FROM returned WHERE loan_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                // Aggregate always returns exactly one row (0 when no repayments exist)
                if (rs.next()) {
                    int alreadyPaid = rs.getInt(1);
                    return Math.max(loanAmount - alreadyPaid, 0);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur calcul solde restant du prêt : " + e.getMessage());
        }
        return loanAmount;
    }

    public int getOutstandingAmountExcluding(String loanId, int loanAmount, String excludeReturnId) {
        String sql = "SELECT COALESCE(SUM(returned_amount), 0) FROM returned "
                   + "WHERE loan_id = ? AND return_id <> ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loanId);
            ps.setString(2, excludeReturnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int alreadyPaid = rs.getInt(1);
                    return Math.max(loanAmount - alreadyPaid, 0);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur calcul solde restant (excl) : " + e.getMessage());
        }
        return loanAmount;
    }

    // CREATE
    public boolean ajouter(Returns r) {
        String sql = "INSERT INTO returned (return_id, loan_id, fully_returned, unpayed, returned_amount, return_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.get_id());
            ps.setString(2, r.get_loan());
            ps.setBoolean(3, r.is_repayed());
            ps.setInt(4, r.get_unpayed());
            ps.setInt(5, r.get_amount());
            ps.setTimestamp(6, Timestamp.valueOf(r.get_date()));

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout return : " + e.getMessage());
            return false;
        }
    }

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

    // UPDATE
    public boolean modifier(Returns r) {
        String sql = "UPDATE returned SET loan_id = ?, fully_returned = ?, unpayed = ?, returned_amount = ?, return_date = ? WHERE return_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.get_loan());
            ps.setBoolean(2, r.is_repayed());
            ps.setInt(3, r.get_unpayed());
            ps.setInt(4, r.get_amount());
            ps.setTimestamp(5, Timestamp.valueOf(r.get_date()));
            ps.setString(6, r.get_id());


            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification return : " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean supprimer(String return_id) {
        String sql = "DELETE FROM returned WHERE return_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, return_id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression return : " + e.getMessage());
            return false;
        }
    }

    //List des PRETE pour chaque SITUATION
    public List<Returns> listerParSituation(boolean fully_returned) {
        List<Returns> liste = new ArrayList<>();
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
                      "JOIN clients c ON l.debtor_id = c.account_id " +
                      "WHERE fully_returned = ?";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, fully_returned);
            ResultSet rs = ps.executeQuery();

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
                liste.add(returned);
            }

        } catch (SQLException e) {
            System.out.println("Erreur listage par situation : " + e.getMessage());
        }
        return liste;
    }

    /** Case-insensitive search across return id, loan id and debtor name. */
    public List<Returns> searchReturns(String query) {
        List<Returns> results = new ArrayList<>();
        String sql = "SELECT "
                   + "r.loan_id, r.return_id, c.first_name, r.return_date, "
                   + "r.returned_amount, l.amount as loan_amount, "
                   + "CASE WHEN fully_returned = TRUE THEN 'fully returned' ELSE 'still in debt' END as status, "
                   + "r.fully_returned, r.unpayed "
                   + "FROM returned r "
                   + "JOIN loans l ON l.loan_id = r.loan_id "
                   + "JOIN clients c ON l.debtor_id = c.account_id "
                   + "WHERE LOWER(r.return_id) LIKE ? "
                   + "OR LOWER(r.loan_id) LIKE ? "
                   + "OR LOWER(c.first_name) LIKE ? "
                   + "OR LOWER(c.last_name) LIKE ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String formatted = "%" + query.toLowerCase().trim() + "%";
            stmt.setString(1, formatted);
            stmt.setString(2, formatted);
            stmt.setString(3, formatted);
            stmt.setString(4, formatted);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Returns(
                        rs.getString("status"),
                        rs.getInt("loan_amount"),
                        rs.getInt("returned_amount"),
                        rs.getInt("unpayed"),
                        rs.getString("return_id"),
                        rs.getString("loan_id"),
                        rs.getString("first_name"),
                        rs.getTimestamp("return_date").toLocalDateTime(),
                        rs.getBoolean("fully_returned")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Returns> searchReturnsBySituation(String query, boolean fullyReturned) {
        List<Returns> results = new ArrayList<>();
        String sql = "SELECT "
                   + "r.loan_id, r.return_id, c.first_name, r.return_date, "
                   + "r.returned_amount, l.amount as loan_amount, "
                   + "CASE WHEN fully_returned = TRUE THEN 'fully returned' ELSE 'still in debt' END as status, "
                   + "r.fully_returned, r.unpayed "
                   + "FROM returned r "
                   + "JOIN loans l ON l.loan_id = r.loan_id "
                   + "JOIN clients c ON l.debtor_id = c.account_id "
                   + "WHERE r.fully_returned = ? "
                   + "AND (LOWER(r.return_id) LIKE ? "
                   + "OR LOWER(r.loan_id) LIKE ? "
                   + "OR LOWER(c.first_name) LIKE ? "
                   + "OR LOWER(c.last_name) LIKE ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String formatted = "%" + query.toLowerCase().trim() + "%";
            stmt.setBoolean(1, fullyReturned);
            stmt.setString(2, formatted);
            stmt.setString(3, formatted);
            stmt.setString(4, formatted);
            stmt.setString(5, formatted);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Returns(
                        rs.getString("status"),
                        rs.getInt("loan_amount"),
                        rs.getInt("returned_amount"),
                        rs.getInt("unpayed"),
                        rs.getString("return_id"),
                        rs.getString("loan_id"),
                        rs.getString("first_name"),
                        rs.getTimestamp("return_date").toLocalDateTime(),
                        rs.getBoolean("fully_returned")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}