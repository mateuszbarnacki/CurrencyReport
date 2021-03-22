package CurrencyReport;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import CurrencyReport.Datamodel.Datasource;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Views/MainWindow.fxml"));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Financial Report");
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        Datasource.getInstance().load();
    }
}
