package server.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Maximilian Schirm
 * @created 08.12.2016
 */

public class ServerGui extends Application{

    private static final String WINDOW_TITLE = "MinoTeX Server 1.0.0";

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(event -> System.exit(0));

        //Loading UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("serverMain.fxml"));
        Pane root = loader.load();
        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.show();
    }
}
