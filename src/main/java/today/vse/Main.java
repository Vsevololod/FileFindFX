package today.vse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL rURL = getClass().getClassLoader().getResource("sample.fxml");
        if(rURL!=null) {
            Parent root = FXMLLoader.load(rURL);
            primaryStage.setTitle("Find Logs");
            primaryStage.setScene(new Scene(root, 900, 600));
            primaryStage.show();
        }else {
            System.err.println("can't find fxml resource");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
