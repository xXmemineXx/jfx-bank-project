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
        String sql = "SELECT * FROM loans JOIN clients ON clients.account_id = loans.debtor_id";

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
                    rs.getString("first_name"),
                    rs.getTimestamp("loan_date").toLocalDateTime()
                );
                LoansList.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LoansList;
    }

    // CREATE
    public boolean ajouter(Loans l) {
        String sql = "INSERT INTO loans (loan_id, debtor_id, amount) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, l.get_id());
            ps.setString(2, l.get_debtor());
            ps.setInt(3, l.get_amount());

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout loan : " + e.getMessage());
            return false;
        }
    }

    public Loans getActiveLoanForClient(String clientId) {
        String sql = "SELECT l.*, c.first_name FROM loans l " +
                     "LEFT JOIN clients c ON c.account_id = l.debtor_id " +
                     "WHERE l.debtor_id = ? " +
                     "AND NOT EXISTS (SELECT 1 FROM returned r " +
                     "                WHERE r.loan_id = l.loan_id AND r.fully_returned = TRUE) " +
                     "ORDER BY l.loan_date DESC LIMIT 1";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Loans(
                        rs.getInt("amount"),
                        rs.getString("loan_id"),
                        rs.getString("debtor_id"),
                        rs.getString("first_name"),
                        rs.getTimestamp("loan_date").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche pret actif : " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean modifier(Loans l) {
        String sql = "UPDATE loans SET debtor_id = ?, amount = ?, loan_date = ? WHERE loan_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, l.get_debtor());
            ps.setInt(2, l.get_amount());
            ps.setTimestamp(3, Timestamp.valueOf(l.get_date()));
            ps.setString(4, l.get_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification loan : " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean supprimer(String loan_id) {
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loan_id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression loan : " + e.getMessage());
            return false;
        }
    }

    /** Case-insensitive search across loan id, debtor id and debtor name. */
    public List<Loans> searchLoans(String query) {
        List<Loans> results = new ArrayList<>();
        String sql = "SELECT * FROM loans JOIN clients ON clients.account_id = loans.debtor_id "
                   + "WHERE LOWER(loans.loan_id) LIKE ? "
                   + "OR LOWER(loans.debtor_id) LIKE ? "
                   + "OR LOWER(clients.first_name) LIKE ? "
                   + "OR LOWER(clients.last_name) LIKE ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String formatted = "%" + query.toLowerCase().trim() + "%";
            stmt.setString(1, formatted);
            stmt.setString(2, formatted);
            stmt.setString(3, formatted);
            stmt.setString(4, formatted);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Loans(
                        rs.getInt("amount"),
                        rs.getString("loan_id"),
                        rs.getString("debtor_id"),
                        rs.getString("first_name"),
                        rs.getTimestamp("loan_date").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}