package CurrencyReport.Datamodel;

import java.time.LocalDate;

public class CurrencyHolder {
    private static final CurrencyHolder instance = new CurrencyHolder();
    private String name;
    private String code;
    private LocalDate beginning;
    private LocalDate end;

    private CurrencyHolder() {}

    public static CurrencyHolder getInstance() {
        return instance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setBeginning(LocalDate beginning) {
        this.beginning = beginning;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public LocalDate getBeginning() {
        return this.beginning;
    }

    public LocalDate getEnd() {
        return this.end;
    }
}
