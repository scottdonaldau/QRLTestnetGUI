package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Transactions class
 *
 * @author Aidan
 */
public class TransactionsController implements Initializable {
    
    @FXML
    AnchorPane transactionsPane;

    private ParentController parent;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void init(ParentController mainController) {
        parent = mainController;
    }
}