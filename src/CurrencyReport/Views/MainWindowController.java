package CurrencyReport.Views;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

/**
 * This class is a controller for main window. It handles 'Aktualna tabela kursów' and
 * 'Dane szczegółowe waluty' buttons.
 */

public class MainWindowController {
    @FXML
    public BorderPane borderPane;

    // This function handles 'Aktualna tabela kursów' button
    @FXML
    public void loadCurrencyTable() {
        WindowLoader loader = new WindowLoader("CurrencyTableWindow");
        loader.load(this.borderPane);
    }

    // This function handles 'Dane szczegółowe waluty' button
    @FXML
    public void loadSingleCurrency() {
        WindowLoader loader = new WindowLoader("SearchDataWindow");
        loader.load(this.borderPane);
    }

}
