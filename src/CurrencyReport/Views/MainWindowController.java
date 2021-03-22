package CurrencyReport.Views;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;


public class MainWindowController {
    @FXML
    public BorderPane borderPane;

    @FXML
    public void loadCurrencyTable() {
        WindowLoader loader = new WindowLoader("CurrencyTableWindow");
        loader.load(this.borderPane);
    }

    @FXML
    public void loadSingleCurrency() {
        WindowLoader loader = new WindowLoader("SearchDataWindow");
        loader.load(this.borderPane);
    }

}
