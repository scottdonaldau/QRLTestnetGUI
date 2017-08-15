/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QRLTestnetWallet;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle; 

/**
 *
 * @author Aidan
 */
public class QRLWallet extends Application {
    
    private double xOffset = 0;
    private double yOffset = 0;
      
    @Override
    public void start(Stage stage) throws Exception {
        
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);

         root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        ContactQRL.connect();
        String thingInfo[] = ContactQRL.getWalletBalance();
        ContactQRL.getInfo();
        launch(args);
    }

    public String[] sendQRL(String fromAddress, String toAddress, String amount) {
        String[] sendQRL;
        sendQRL = ContactQRL.sendQRL(fromAddress, toAddress, amount);
        return sendQRL;
    }
}