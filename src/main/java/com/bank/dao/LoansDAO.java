package com.bank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bank.database.DbConnection;
import com.bank.models.Loans;

public class LoansDAO
{
    // CREATE
    public boolean ajouter(Loans l) {
        String sql = "INSERT INTO loans (loan_id, debtor_id, amount, loan_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, l.get_id());
            ps.setString(2, l.get_debtor());
            ps.setInt(3, l.get_amount());
            ps.setTimestamp(4, Timestamp.valueOf(l.get_date()));

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout loan : " + e.getMessage());
            return false;
        }
    }

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
                    rs.getTimestamp("loan_date").toLocalDateTime()
                );
                LoansList.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LoansList;
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

    //BENEFICE BANQUE
    public double calculerBeneficeBanque() {
        String sql = "SELECT SUM(amount) * 0.10 AS benefice FROM loans";
        double benefice = 0;

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                benefice = rs.getDouble("benefice"); //rs.getDouble("benefice") récupere la valeur de sql
            }

        } catch (SQLException e) {
            System.out.println("Erreur calcul bénéfice : " + e.getMessage());
        }
        return benefice;
    }

    
}