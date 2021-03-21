package sample;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

public class SearchDataController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public DatePicker beginDate;
    @FXML
    public DatePicker endDate;
    @FXML
    public ComboBox<String> comboBox;

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

    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("MainWindow");
        loader.load(this.borderPane);
    }

    @FXML
    public void loadSpecialCurrencyPage() {
        if (this.beginDate.getValue().compareTo(this.endDate.getValue()) > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Błąd daty");
            alert.setContentText("Błędny zakres dat!");

            Optional<ButtonType> response = alert.showAndWait();
            if (response.isPresent()) {
                alert.close();
            }
        } else {
            setSearchData();
            WindowLoader loader = new WindowLoader("SpecialCurrencyWindow");
            loader.load(this.borderPane);
        }
    }

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
}
