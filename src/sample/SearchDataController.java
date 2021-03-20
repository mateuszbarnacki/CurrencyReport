package sample;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private final List<String> currencyName = new ArrayList<>();
    private final List<String> currencyCodes = new ArrayList<>();

    public void initialize() {
        this.beginDate.setValue(LocalDate.now());
        this.endDate.setValue(LocalDate.now());
        prepareListOfCurrencies("http://api.nbp.pl/api/exchangerates/tables/A/");
        prepareListOfCurrencies("http://api.nbp.pl/api/exchangerates/tables/B/");
        this.comboBox.setValue(this.currencyName.get(0));
        this.comboBox.setItems(FXCollections.observableList(this.currencyName));
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


    private void prepareListOfCurrencies(String address) {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            int code = connection.getResponseCode();

            if (code != 200) {
                System.out.println("Couldn't get data!");
            } else {
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    String[] tempArr = line.split(",");
                    for (String elem : tempArr) {
                        String[] test = elem.split(":");
                        if (test[0].compareTo("{\"currency\"") == 0) {
                            this.currencyName.add(test[1].substring(1, test[1].length()-1));
                        } else if (test[0].compareTo("\"rates\"") == 0) {
                            this.currencyName.add(test[2].substring(1, test[2].length()-1));
                        } else if (test[0].compareTo("\"code\"") == 0) {
                            this.currencyCodes.add(test[1].substring(1, 4));
                        }
                    }
                }
                input.close();
            }
        } catch (MalformedURLException e) {
            System.out.println("Couldn't get data: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException while getting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setSearchData() {
        CurrencyHolder.getInstance().setName(this.comboBox.getSelectionModel().getSelectedItem());
        int position = 0;
        for (String elem : this.currencyName) {
            if (elem.compareTo(this.comboBox.getSelectionModel().getSelectedItem()) == 0) {
                CurrencyHolder.getInstance().setCode(this.currencyCodes.get(position));
            }
            position++;
        }
        CurrencyHolder.getInstance().setBeginning(this.beginDate.getValue());
        CurrencyHolder.getInstance().setEnd(this.endDate.getValue());
    }
}
