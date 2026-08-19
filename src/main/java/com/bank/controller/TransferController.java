package com.bank.controller;

import com.bank.dao.TransferDAO;
import com.bank.helpers.ActionCard;
import com.bank.models.Transfer; 
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TransferController implements ActionCard {
    @FXML private HBox cardRoot;
    @FXML private Label senderName;
    @FXML private Label receiverName;
    @FXML private Label transferDateLabel;
    @FXML private Label transferedAmountLabel;
    @FXML private VBox download;

    private final TransferDAO transferDAO = new TransferDAO();
    private Transfer currentTransfer;

    public void setTransferData(Transfer transfer) {
        this.currentTransfer = transfer;
    }

    // download function
    @FXML
    private void handleDownloadPdf() { 
        if (currentTransfer == null) {
            System.out.println("Error: No transfer data linked to this card layout component!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/transferPdf.fxml"));
            Parent receiptRoot = loader.load();

            TransferPdfController receiptController = loader.getController();
            receiptController.initData(currentTransfer);

            javafx.scene.Scene dummyScene = new javafx.scene.Scene(receiptRoot);
            
            receiptRoot.applyCss();
            receiptRoot.layout();

            // Take the snapshot now that the elements are fully painted in memory
            SnapshotParameters params = new SnapshotParameters();
            WritableImage snapshot = receiptRoot.snapshot(params, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            Stage currentStage = (Stage) download.getScene().getWindow();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le reçu de virement");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
            fileChooser.setInitialFileName("Recu_Virement_" + currentTransfer.get_id() + ".pdf");
            File destinationFile = fileChooser.showSaveDialog(currentStage);

            if (destinationFile != null) {
                try (PDDocument document = new PDDocument()) {
                    // Set the PDF document page dimension dynamically to match your FXML size
                    PDRectangle pageSize = new PDRectangle(
                        (float) receiptRoot.getLayoutBounds().getWidth(), 
                        (float) receiptRoot.getLayoutBounds().getHeight()
                    );
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);

                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0);
                    }

                    document.save(destinationFile);
                    System.out.println("Reçu PDF enregistré avec succès !");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void populateCardData(Object dataRecord) {
        Transfer transfer = (Transfer) dataRecord; 
        
        setTransferData(transfer); 
        
        senderName.setText("sender : " + transfer.get_sender_name());
        receiverName.setText("receiver : " + transfer.get_receiver_name());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        transferDateLabel.setText(transfer.get_date().format(formatter));
        
        transferedAmountLabel.setText(String.format("%,d Ar", transfer.get_amount()));
    }

    // trash icon
    @FXML
    private void handleDelete() {
        if (currentTransfer == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete this transfer?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            boolean success = transferDAO.supprimer(currentTransfer.get_id());
            if (success && cardRoot.getParent() instanceof Pane parent) {
                parent.getChildren().remove(cardRoot);
            } else if (!success) {
                new Alert(Alert.AlertType.ERROR, "Could not delete this transfer.").showAndWait();
            }
        }
    }

    // pencil icon
    @FXML
    private void handleEdit() {
        if (currentTransfer == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/transferForm.fxml"));
            Parent root = loader.load();

            TransferFormController formController = loader.getController();
            formController.setEditMode(currentTransfer, () -> populateCardData(currentTransfer));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Edit Transfer");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
