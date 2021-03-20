package sample;

import javafx.beans.property.SimpleStringProperty;

public class Currency {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty code = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();

    public Currency(String name, String code, String value) {
        this.name.set(name);
        this.code.set(code);
        this.value.set(value);
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

    @Override
    public String toString() {
        return "Name: " + this.name.get() + " Code: " + this.code.get() + " Value: " + this.value.get();
    }
}
