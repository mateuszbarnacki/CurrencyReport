package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    public Label searchDateLabel;
    @FXML
    public Label searchValueLabel;
    private final List<String> dates = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    private final List<CurrencyHistory> history = new ArrayList<>();


    public void initialize() {
        this.nameLabel.setText(CurrencyHolder.getInstance().getName());
        this.codeLabel.setText(CurrencyHolder.getInstance().getCode());
        this.specialDate.setValue(CurrencyHolder.getInstance().getEnd());
        if (CurrencyHolder.getInstance().getBeginning().compareTo(CurrencyHolder.getInstance().getEnd()) == 0) {
            searchCurrencyDataForSpecialDate();
        } else {
            searchCurrencyDataForPeriodOfTime();
        }
        Task<ObservableList<CurrencyHistory>> task = new GetHistory();
        this.historyTableView.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    @FXML
    public void loadPreviousPage() {
        WindowLoader loader = new WindowLoader("SearchDataWindow");
        loader.load(this.borderPane);
    }

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
        yAxis.setLowerBound(min - 0.1);
        yAxis.setUpperBound(max + 0.1);
        yAxis.setTickUnit((max-min) * 0.1);
        yAxis.setLabel("Currency price");
        LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
        lineChart.setTitle(CurrencyHolder.getInstance().getName().substring(0, 1).toUpperCase() + CurrencyHolder.getInstance().getName().substring(1));
        XYChart.Series series = new XYChart.Series();
        for (CurrencyHistory elem : this.history) {
            series.getData().add(new XYChart.Data<String, Number>(elem.getDate(), Double.parseDouble(elem.getValue())));
        }
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
        pane.setContent(lineChart);
        dialog.setDialogPane(pane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Optional<ButtonType> response = dialog.showAndWait();

        if(response.isPresent() && (response.get() == ButtonType.OK)) dialog.close();
    }

    @FXML
    public void searchSpecialValue(ActionEvent event) {
        if (this.specialDate.getValue().compareTo(CurrencyHolder.getInstance().getEnd()) > 0 || this.specialDate.getValue().compareTo(CurrencyHolder.getInstance().getBeginning()) < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błędna data");
            alert.setHeaderText(null);
            alert.setContentText("Wybrano date spoza zakresu wyszukanej historii!");

            Optional<ButtonType> response = alert.showAndWait();
            if (response.isPresent()) {
                alert.close();
            }
        } else {
            for (CurrencyHistory elem : this.history) {
                if (elem.getDate().compareTo(this.specialDate.getValue().toString()) == 0) {
                    this.searchDateLabel.setText(elem.getDate());
                    this.searchValueLabel.setText(elem.getValue());
                }
            }
        }
    }

    private void searchCurrencyDataForSpecialDate() {
        Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
        executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + CurrencyHolder.getInstance().getBeginning().toString() + "/");
    }

    private void searchCurrencyDataForPeriodOfTime() {
        final long dayConst = 24*3600*1000;
        long diffOfDays;
        Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
        Date beginning;
        Date end;
        MyPeriod myPeriod;
        LocalDate temp;
        List<MyPeriod> myPeriods = new ArrayList<>();
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
            executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + myPeriods.get(i).getBeginning().toString() + "/" + myPeriods.get(i).getEnd().toString() + "/");
        }
    }

    private void executeQuery(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            int code = connection.getResponseCode();

            if (code == 404) {
                Currency currency = Datasource.getInstance().getCurrencyByName(CurrencyHolder.getInstance().getName());
                CurrencyHolder.getInstance().setBeginning(CurrencyHolder.getInstance().getBeginning().minusDays(1));
                executeQuery("http://api.nbp.pl/api/exchangerates/rates/" + currency.getType() + "/" + currency.getCode() + "/" + CurrencyHolder.getInstance().getBeginning().toString() + "/" + CurrencyHolder.getInstance().getEnd().toString() + "/");
                return;
            } else if (code != 200) {
                System.out.println("Couldn't get data!");
            } else {
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
            if (this.dates.size() == this.values.size()) {
                for (int i = 0; i < this.dates.size(); i++) {
                    this.history.add(new CurrencyHistory(this.dates.get(i), this.values.get(i)));
                }
                CurrenciesData.getInstance().copy(this.history);
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class GetHistory extends Task {
    @Override
    protected ObservableList<CurrencyHistory> call() throws Exception {
        return FXCollections.observableList(CurrenciesData.getInstance().getCurrencies());
    }
}
