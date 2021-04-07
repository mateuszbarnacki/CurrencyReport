package CurrencyReport.Datamodel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Datasource {
    private static final Datasource instance = new Datasource();
    private final List<Currency> currencies = new ArrayList<>();

    private Datasource() {}

    public static Datasource getInstance() {
        return instance;
    }

    public void load() {
        loadDataFromSingleTable("A");
        loadDataFromSingleTable("B");
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public Currency getCurrencyByCode(String code) {
        Currency result = null;
        for (Currency currency : currencies) {
            if (currency.getCode().compareTo(code) == 0) result = currency;
        }
        return result;
    }

    public Currency getCurrencyByName(String name) {
        Currency result = null;
        for (Currency currency : currencies) {
            if (currency.getName().compareTo(name) == 0) result = currency;
        }
        return result;
    }

    public int getIndexByName(String name) {
        int idx = -1;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getName().compareTo(name) == 0) idx = i;
        }
        return idx;
    }

    public int getIndexByCode(String code) {
        int idx = -1;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getCode().compareTo(code) == 0) idx = i;
        }
        return idx;
    }

    public Currency getCurrencyByIndex(int idx) {
        Currency result = null;
        for (int i = 0; i < currencies.size(); i++) {
            if (i == idx) result = currencies.get(i);
        }
        return result;
    }

    public List<String> getListOfCurrenciesNames() {
        List<String> names = new ArrayList<>();
        for (Currency currency : currencies) {
            names.add(currency.getName());
        }
        return names;
    }

    private void loadDataFromSingleTable(String type) {
        List<String> currencyNames = new ArrayList<>();
        List<String> currencyCodes = new ArrayList<>();
        List<String> currencyMidValues = new ArrayList<>();

        try {
            URL url = new URL("http://api.nbp.pl/api/exchangerates/tables/" + type + "/");
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
                            currencyNames.add(attributes[1].substring(1, attributes[1].length()-1));
                        } else if (attributes[0].compareTo("\"rates\"") == 0) {
                            currencyNames.add(attributes[2].substring(1, attributes[2].length()-1));
                        } else if (attributes[0].compareTo("\"mid\"") == 0) {
                            if (attributes[1].substring(0, 6).matches("^\\d+\\.\\d+$")) currencyMidValues.add(attributes[1].substring(0, 6));
                        } else if (attributes[0].compareTo("\"code\"") == 0) {
                            currencyCodes.add(attributes[1].substring(1, 4));
                        }
                    }
                }
                if (currencyCodes.size() == currencyNames.size() && currencyNames.size() == currencyMidValues.size()) {
                    Currency currency;
                    for (int i = 0; i < currencyCodes.size(); i++) {
                        currency = new Currency(currencyNames.get(i), currencyCodes.get(i), currencyMidValues.get(i), type);
                        this.currencies.add(currency);
                    }
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
    }
}
