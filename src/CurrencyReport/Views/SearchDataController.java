package CurrencyReport.Views;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import CurrencyReport.Datamodel.CurrencyHolder;
import CurrencyReport.Datamodel.Datasource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

/**
 * This class is a controller for 'Dane szczegółowe waluty' window.
 */

public class SearchDataController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public DatePicker beginDate;
    @FXML
    public DatePicker endDate;
    @FXML
    public ComboBox<String> comboBox;

    // This function initialize date pickers and combo box.
    // In date pickers it excludes weekends, because there was no currency rates in these days.
    // Combo box is filled by currency names from datasource.
    public void initialize() {
        this.beginDate.setDayCellFactory(picker -> new DateCell()    {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY || date.compareTo(LocalDate.now()) > 0 || date.compareTo(LocalDate.of(2002, 1, 2)) < 0);
            }
        });
        this.endDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY || date.compareTo(LocalDate.now()) > 0 || date.compareTo(LocalDate.of(2002, 1, 2)) < 0);
            }
        });
        this.comboBox.setValue(Datasource.getInstance().getCurrencyByIndex(0).getName());
        this.comboBox.setItems(FXCollections.observableList(Datasource.getInstance().getListOfCurrenciesNames()));
    }

    // This function handle 'Powrót' button.
    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("MainWindow");
        loader.load(this.borderPane);
    }

    // This function handles 'Zatwierdź' button
    @FXML
    public void loadSpecialCurrencyPage() {
        if (this.beginDate.getValue() == null || this.endDate.getValue() == null) {
            alertCreator("Błąd daty", "Brak wybranej daty!");
        } else if (this.beginDate.getValue().compareTo(this.endDate.getValue()) > 0) {
            alertCreator("Błąd daty", "Błędny zakres dat!");
        } else {
            setSearchData();
            WindowLoader loader = new WindowLoader("SpecialCurrencyWindow");
            loader.load(this.borderPane);
        }
    }

    // This function stores a selected currency data in CurrencyHolder class.
    private void setSearchData() {
        CurrencyHolder.getInstance().setName(this.comboBox.getSelectionModel().getSelectedItem());
        int position = 0;
        for (String elem : Datasource.getInstance().getListOfCurrenciesNames()) {
            if (elem.compareTo(this.comboBox.getSelectionModel().getSelectedItem()) == 0) {
                CurrencyHolder.getInstance().setCode(Datasource.getInstance().getCurrencyByIndex(position).getCode());
            }
            position++;
        }
        CurrencyHolder.getInstance().setBeginning(this.beginDate.getValue());
        CurrencyHolder.getInstance().setEnd(this.endDate.getValue());
    }

    // This function prepares alert dialog window.
    private void alertCreator(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(content);

        Optional<ButtonType> response = alert.showAndWait();
        if (response.isPresent()) {
            alert.close();
        }
    }

}
