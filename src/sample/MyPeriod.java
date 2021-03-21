package sample;

import java.time.LocalDate;

public class MyPeriod {
    private final LocalDate beginning;
    private final LocalDate end;

    public MyPeriod(LocalDate beginning, LocalDate end) {
        this.beginning = beginning;
        this.end = end;
    }

    public LocalDate getBeginning() {
        return this.beginning;
    }

    public LocalDate getEnd() {
        return this.end;
    }
}
