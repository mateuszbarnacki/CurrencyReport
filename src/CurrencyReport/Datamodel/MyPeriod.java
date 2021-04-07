package CurrencyReport.Datamodel;

import java.time.LocalDate;

/**
 * This class contains information about beginning and end of 80-day period used in
 * searchCurrencyDataForPeriodOfTime() - SpecialCurrencyController.
 */

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
