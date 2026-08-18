package com.bank;

import com.bank.dao.ClientDAO;
import com.bank.models.Client;

/*public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // This loads the FXML file you exported from Scene Builder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/MainView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Bank app");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
*/
public class App {
    /*
    public static void main(String[] args) {
        ClientDAO dao = new ClientDAO();

        // Test ajout
        Client c = new Client("200543", "RAKOTO", "Bernard", "rakoto@mail.com", "0348407183", 20000);
        dao.ajouter(c);

        //UPDATE

        //Afficher AVANT modification
        System.out.println("=== AVANT ===");
        dao.getAllClients().forEach(System.out::println);

        //Créer un objet avec les nouvelles valeurs (même numCompteClient, autres champs modifiés)
        Client c = new Client("200543", "RAKOTO", "Bernard", "nouveau@mail.com", "0340300330", 20000);
        boolean succes = dao.modifier(c);

        System.out.println("Modification réussie : " + succes);

        //Afficher APRÈS modification
        System.out.println("=== APRÈS ===");
        dao.getAllClients().forEach(System.out::println);

        //dao.supprimer("200543");

        // Test listage
        //dao.getAllClients().forEach(System.out::println);
    }
 

    public static void main(String[] args) {
        LoansDAO dao = new LoansDAO();

            //ajout
            //Loans l = new Loans(10000, "00000A", "200543", LocalDateTime.now());
            //dao.ajouter(l);
            
            //bénéfice
            //LoansDAO loansDao = new LoansDAO();
            //System.out.println("Bénéfice banque : " + loansDao.calculerBeneficeBanque() + " Ar");
            
    }*/
    
    //Lite de tout les PRETES pour chaque SITUATION
    /*public static void main(String[] args){
        ReturnsDAO returnsDao = new ReturnsDAO();

        System.out.println("=== Prêts entièrement remboursés ===");
        returnsDao.listerParSituation(true).forEach(System.out::println);

        System.out.println("=== Prêts partiellement remboursés ===");
        returnsDao.listerParSituation(false).forEach(System.out::println);
    }*/
    

}