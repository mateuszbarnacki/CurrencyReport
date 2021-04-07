package CurrencyReport.Datamodel;

import javafx.beans.property.SimpleStringProperty;

public class CurrencyHistory {
    private final SimpleStringProperty date = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();

    public CurrencyHistory(String date, String value) {
        this.date.set(date);
        this.value.set(value);
    }

    public String getDate() {
        return this.date.get();
    }

    public String getValue() {
        return this.value.get();
    }


    @Override
    public String toString() {
        return this.date.get() + ": " + this.value.get();
    }
}
