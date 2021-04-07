package CurrencyReport.Datamodel;

import javafx.beans.property.SimpleStringProperty;

public class Currency {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty code = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();
    private final String type;

    public Currency(String name, String code, String value, String type) {
        this.name.set(name);
        this.code.set(code);
        this.value.set(value);
        this.type = type;
    }

    public String getName() {
        return this.name.get();
    }

    public String getCode() {
        return this.code.get();
    }

    public String getValue() {
        return this.value.get();
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "Name: " + this.name.get() + " Code: " + this.code.get() + " Value: " + this.value.get();
    }
}
