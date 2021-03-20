package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrencyTableWindowController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public TableView<Currency> currencyTableView;
    private final List<String> currencyNames = new ArrayList<>();
    private final List<String> currencyCodes = new ArrayList<>();
    private final List<String> currencyMidValues = new ArrayList<>();
    private final List<Currency> currencies = new ArrayList<>();

    public void initialize() {
        try {
            URL url = new URL("http://api.nbp.pl/api/exchangerates/tables/A/");
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
                    String[] data = line.split(",");
                    for (String elem : data) {
                        String[] attributes = elem.split(":");
                        if (attributes[0].compareTo("{\"currency\"") == 0) {
                            this.currencyNames.add(attributes[1].substring(1, attributes[1].length()-1));
                        } else if (attributes[0].compareTo("\"rates\"") == 0) {
                            this.currencyNames.add(attributes[2].substring(1, attributes[2].length()-1));
                        } else if (attributes[0].compareTo("\"mid\"") == 0) {
                            if (attributes[1].substring(0, 6).matches("^\\d+\\.\\d+$")) this.currencyMidValues.add(attributes[1].substring(0, 6));
                        } else if (attributes[0].compareTo("\"code\"") == 0) {
                            this.currencyCodes.add(attributes[1].substring(1, 4));
                        }
                    }
                }
                if (this.currencyCodes.size() == this.currencyNames.size() && this.currencyNames.size() == this.currencyMidValues.size()) {
                    Currency currency;
                    for (int i = 0; i < this.currencyCodes.size(); i++) {
                        currency = new Currency(this.currencyNames.get(i), this.currencyCodes.get(i), this.currencyMidValues.get(i));
                        this.currencies.add(currency);
                    }
                    CurrenciesData.getInstance().copy(this.currencies);
                }
                input.close();
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }

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
        return FXCollections.observableList(CurrenciesData.getInstance().getCurrencies());
    }
}