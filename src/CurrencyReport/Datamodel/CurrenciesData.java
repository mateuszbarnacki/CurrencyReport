package CurrencyReport.Datamodel;

import java.util.ArrayList;
import java.util.List;

public class CurrenciesData {
    private static final CurrenciesData instance = new CurrenciesData();
    private final List currencies = new ArrayList<>();

    private CurrenciesData() {}

    public static CurrenciesData getInstance() {
        return instance;
    }

    public void copy(List currencies) {
        this.currencies.clear();
        for (var elem : currencies) {
            this.currencies.add(elem);
        }
    }

    public List getCurrencies() {
        return this.currencies;
    }
}
