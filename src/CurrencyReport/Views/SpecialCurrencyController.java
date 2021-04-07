package CurrencyReport.Views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import CurrencyReport.Datamodel.*;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This class is a controller for 'Dane szczegółowe waluty -> Zatwierdź' window.
 */

public class SpecialCurrencyController {
    @FXML
    public BorderPane borderPane;
    @FXML
    public Label nameLabel;
    @FXML
    public Label codeLabel;
    @FXML
    public TableView<CurrencyHistory> historyTableView;
    @FXML
    public DatePicker specialDate;
    @FXML
    public HBox searchSpecialDateBox;
    @FXML
    public Label searchDateLabel;
    @FXML
    public Label searchValueLabel;
    private final List<String> dates = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    private final List<CurrencyHistory> history = new ArrayList<>();
    private LocalDate beginning;
    private LocalDate end;

    // This function initialize currency name and currency code labels, download the appropriate data
    // from API NBP website and set end date as the default value of date picker.
    public void initialize() {
        this.beginning = CurrencyHolder.getInstance().getBeginning();
        this.end = CurrencyHolder.getInstance().getEnd();
        this.nameLabel.setText(CurrencyHolder.getInstance().getName());
        this.codeLabel.setText(CurrencyHolder.getInstance().getCode());
        this.specialDate.setValue(CurrencyHolder.getInstance().getEnd());

        // Check whether the beginning date and end date are the same
        if (CurrencyHolder.getInstance().getBeginning().compareTo(CurrencyHolder.getInstance().getEnd()) == 0) {
            searchCurrencyDataForSpecialDate();
        } else {
            searchCurrencyDataForPeriodOfTime();
        }

        this.specialDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(beginning) < 0 || date.compareTo(end) > 0 || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            }
        });

        this.searchSpecialDateBox.setVisible(false);
        Task<ObservableList<CurrencyHistory>> task = new GetHistory();
        this.historyTableView.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    // This function handles 'Powrót' button
    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("SearchDataWindow");
        loader.load(this.borderPane);
    }

    // This function handles 'Rysuj wykres' button. It draws a chart based on the downloaded data
    // presented in the table.
    @FXML
    public void drawChart(ActionEvent event) {
        double max = 0.0, min = 10.0;
        for (CurrencyHistory elem : this.history) {
            if (Double.parseDouble(elem.getValue()) > max) {
                max = Double.parseDouble(elem.getValue());
            } else if (Double.parseDouble(elem.getValue()) < min) {
                min = Double.parseDouble(elem.getValue());
            }
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        DialogPane pane = new DialogPane();
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Dates");
        yAxis.setAutoRanging(false);
        if (min == 10.0) {
            min = max - 0.1;
        }
        yAxis.setLowerBound(min);
        yAxis.setUpperBound(max);
        if (max-min > 1.0) {
            yAxis.setTickUnit(0.5);
        } else if (max-min > 0.1 && max-min < 1.0) {
            yAxis.setTickUnit(0.1);
        } else {
            yAxis.setTickUnit(0.01);
        }
        yAxis.setLabel("Currency price");
        LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
        lineChart.setTitle(CurrencyHolder.getInstance().getName().substring(0, 1).toUpperCase() + CurrencyHolder.getInstance().getName().substring(1));
        XYChart.Series series = new XYChart.Series();
        for (CurrencyHistory elem : this.history) {
            series.getData().add(new XYChart.Data<String, Number>(elem.getDate(), Double.parseDouble(elem.getValue())));
        }
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
        if (series.getData().size() > 12) {
            lineChart.setCreateSymbols(false);
        }
        pane.setContent(lineChart);
        dialog.setDialogPane(pane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Optional<ButtonType> response = dialog.showAndWait();

        if(response.isPresent() && (response.get() == ButtonType.OK)) dialog.close();
    }

    // This function handles 'Szukaj' button. It search for a single currency rate from a specific
    // date.
    @FXML
    public void searchSpecialValue(ActionEvent event) {
        if (this.specialDate.getValue().compareTo(this.end) > 0 || this.specialDate.getValue().compareTo(this.beginning) < 0) {
            alertCreator("Błędna data", "Wybrano date spoza zakresu wyszukanej historii!");
        } else {
            boolean isExists = false;
            for (CurrencyHistory elem : this.history) {
                if (elem.getDate().compareTo(this.specialDate.getValue().toString()) == 0) {
                    this.searchDateLabel.setText(elem.getDate());
                    this.searchValueLabel.setText(elem.getValue());
                    isExists = true;
                }
            }
            if (!isExists) {
                alertCreator("Błędna data", "Brak danych dla wybranej daty! \nBłąd spowodowany brakiem notowania w danym dniu \nkalendarzowym.");
            } else {
                this.searchSpecialDateBox.setVisible(true);
            }
        }
    }

    // This function create a query, which returns a currency rate from a single day.
    private void searchCurrencyDataForSpecialDate() {
        Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
        executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + CurrencyHolder.getInstance().getBeginning().toString() + "/");
        CurrenciesData.getInstance().copy(this.history);
    }

    // This function create a query, which returns a currency rates from a specific period of time.
    private void searchCurrencyDataForPeriodOfTime() {
        final long dayConst = 24*3600*1000;
        long diffOfDays;
        Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
        Date beginning;
        Date end;
        MyPeriod myPeriod;
        LocalDate temp;
        List<MyPeriod> myPeriods = new ArrayList<>();
        // This loop divide a whole period of time into 80-days periods. It stores all these periods
        // in a list.
        do {
            beginning = Date.from(CurrencyHolder.getInstance().getBeginning().atStartOfDay(ZoneId.systemDefault()).toInstant());
            end = Date.from(CurrencyHolder.getInstance().getEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
            diffOfDays = (end.getTime() - beginning.getTime()) / dayConst;
            if (diffOfDays <= 80L) {
                myPeriod = new MyPeriod(beginning.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            } else {
                temp = CurrencyHolder.getInstance().getEnd();
                CurrencyHolder.getInstance().setEnd(CurrencyHolder.getInstance().getEnd().minusDays(80));
                myPeriod = new MyPeriod(CurrencyHolder.getInstance().getEnd(), temp);
            }
            myPeriods.add(myPeriod);
        } while (diffOfDays > 80L);
        int idx = myPeriods.size()-1;
        for (int i = idx; i >= 0; i--) {
            // If the currency rate was unavailable from the beginnig date it change the beginning date.
            if (this.beginning.compareTo(myPeriods.get(i).getBeginning()) > 0) {
                this.beginning = myPeriods.get(i).getBeginning();
            }
            CurrencyHolder.getInstance().setBeginning(myPeriods.get(i).getBeginning());
            CurrencyHolder.getInstance().setEnd(myPeriods.get(i).getEnd());
            executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + myPeriods.get(i).getBeginning().toString() + "/" + myPeriods.get(i).getEnd().toString() + "/");
        }
        // Downloaded data is stored in CurrenciesData class
        CurrenciesData.getInstance().copy(this.history);
    }

    // This function executes the query and download the data from API NBP website.
    private void executeQuery(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            int code = connection.getResponseCode();

            // Code 404 mean that data are unavailable. It tries to get data from previous day.
            if (code == 404) {
                Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
                CurrencyHolder.getInstance().setBeginning(CurrencyHolder.getInstance().getBeginning().minusDays(1));
                if (this.beginning.compareTo(CurrencyHolder.getInstance().getBeginning()) > 0) {
                    this.beginning = CurrencyHolder.getInstance().getBeginning();
                }
                executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + CurrencyHolder.getInstance().getBeginning().toString() + "/" + CurrencyHolder.getInstance().getEnd().toString() + "/");
            } else if (code != 200) { // If response code is different than 404 this method prints error message
                System.out.println("Couldn't get data!");
            } else { // Method gets the data and stores it in appropriate lists.
                BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while((line = input.readLine()) != null) {
                    String[] data = line.split(",");
                    for (String elem : data) {
                        String[] attributes = elem.split(":");
                        if (attributes[0].compareTo("\"effectiveDate\"") == 0) {
                            this.dates.add(attributes[1].substring(1, attributes[1].length()-1));
                        } else if (attributes[0].compareTo("\"mid\"") == 0) {
                            if (attributes[1].substring(0, 6).matches("^\\d+\\.\\d+$")) this.values.add(attributes[1].substring(0, 6));
                        }
                    }
                }
                input.close();
            }
            // Currency rates are added to history lists, which is the main container for all searched data
            if (this.dates.size() == this.values.size()) {
                for (int i = 0; i < this.dates.size(); i++) {
                    this.history.add(new CurrencyHistory(this.dates.get(i), this.values.get(i)));
                }
            }
            // Clear other lists
            this.dates.clear();
            this.values.clear();
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void alertCreator(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> response = alert.showAndWait();
        if (response.isPresent()) {
            alert.close();
        }
    }
}

/**
 * This class loads searched data.
 */
class GetHistory extends Task {
    @Override
    protected ObservableList<CurrencyHistory> call() throws Exception {
        return FXCollections.observableList(CurrenciesData.getInstance().getCurrencies());
    }
}
