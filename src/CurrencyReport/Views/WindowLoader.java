package CurrencyReport.Views;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * This class loads FXML views.
 */

public class WindowLoader {
    private final String filename;

    public WindowLoader (String name) {
        this.filename = name;
    }

    // This function gets borderPane which is the node. It sets the appropriate window
    // using setRoot() method.
    public boolean load(BorderPane borderPane) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(this.filename + ".fxml"));

        try {
            borderPane.getScene().setRoot(fxmlLoader.load());
            return true;
        } catch (IOException e) {
            System.out.println("Couldn't load " + this.filename + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
