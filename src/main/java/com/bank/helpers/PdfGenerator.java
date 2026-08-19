import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
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

public class PdfGenerator {

    public void exportFxmlToPdf(String fxmlPath, Stage stage) {
        try {
            // 1. Charger la vue FXML en mémoire (sans forcément l'afficher à l'écran)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Prendre une capture d'écran (Snapshot) de la vue FXML
            SnapshotParameters params = new SnapshotParameters();
            WritableImage snapshot = root.snapshot(params, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            // 3. Ouvrir une boîte de dialogue pour enregistrer le fichier sur la machine locale
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
            fileChooser.setInitialFileName("export_vue.pdf");
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // 4. Créer le document PDF avec PDFBox
                try (PDDocument document = new PDDocument()) {
                    // Adapter la taille de la page PDF à la taille de la vue FXML
                    PDRectangle pageSize = new PDRectangle((float) root.getLayoutBounds().getWidth(), (float) root.getLayoutBounds().getHeight());
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);

                    // Convertir l'image JavaFX pour PDFBox
                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

                    // Dessiner l'image sur le PDF
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0);
                    }

                    // Sauvegarder le fichier sur le disque
                    document.save(file);
                }
                System.out.println("PDF généré avec succès à l'emplacement : " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
