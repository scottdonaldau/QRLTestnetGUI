/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Aidan
 */
public class AboutController implements Initializable {

    @FXML
    AnchorPane aboutPane;

    private ParentController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }   

    public void init(ParentController mainController) {
        parent = mainController;
    }
}
