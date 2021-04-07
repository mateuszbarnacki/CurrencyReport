package CurrencyReport.Views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import CurrencyReport.Datamodel.Currency;
import CurrencyReport.Datamodel.Datasource;

/**
 * This class is a controller for 'Aktualna tabela kursów' window.
 * It loads a data of current rates of all currencies.
 */

public class CurrencyTableWindowController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public TableView<Currency> currencyTableView;

    // This function prepare TableView using downloaded data about all current currency rates
    public void initialize() {
        Task<ObservableList<Currency>> task = new GetCurrencies();
        this.currencyTableView.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    // This function handles 'Powrót' button
    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("MainWindow");
        loader.load(this.borderPane);
    }
}

/**
 * This class gets appropriate data from Datasource
 */
class GetCurrencies extends Task {
    @Override
    protected ObservableList<Currency> call() throws Exception {
        return FXCollections.observableList(Datasource.getInstance().getCurrencies());
    }
}