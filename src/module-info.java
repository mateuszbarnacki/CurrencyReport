module FinancialReport {
    requires javafx.controls;
    requires javafx.fxml;

    opens CurrencyReport.Datamodel;
    opens CurrencyReport.Views;
    opens CurrencyReport;
}