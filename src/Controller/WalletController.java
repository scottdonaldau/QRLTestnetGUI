package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Aidan
 */
public class WalletController implements Initializable {

    @FXML
    AnchorPane walletPane;

    private ParentController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        System.out.println("WALLET CONTROLLER");
        try {
            //walletPane.setLayoutX(248);
            //walletPane.setLayoutY(101);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void testButtonClicked(MouseEvent event) {
        System.out.println("This is inside the wallet controller!");
    }

    public void setVisibility(Boolean visible) {
        System.out.println("CHANGING VISIBILITY");
        walletPane.setVisible(visible);
    }
    /*
    public void init(ParentController mainController) {
        System.out.println("INITIALISING");
    }
     */
}
