package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class CurrencyTableWindowController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public TableView<Currency> currencyTableView;

    public void initialize() {
        Task<ObservableList<Currency>> task = new GetCurrencies();
        this.currencyTableView.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("MainWindow");
        loader.load(this.borderPane);
    }
}


class GetCurrencies extends Task {
    @Override
    protected ObservableList<Currency> call() throws Exception {
        return FXCollections.observableList(Datasource.getInstance().getCurrencies());
    }
}