package Model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Aidan
 */
public class QRLWallet extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    Boolean searching = false;

    @Override
    public void start(Stage stage) throws Exception {
        //startTask();

        Parent root = FXMLLoader.load(getClass().getResource("/View/FXMLParent.fxml"));
        //stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);

        stage.getIcons().add(new Image("images/icon.png"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        //ContactQRL.connect();
        Application.launch(args);
    }
}