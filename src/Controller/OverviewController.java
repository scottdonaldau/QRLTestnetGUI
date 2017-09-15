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
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Aidan
 */
public class OverviewController implements Initializable {

    @FXML
    AnchorPane overviewPane;
    @FXML
    private Label balanceLabel, balanceLabelQRL, balanceSpendableLabel, balanceUnconfirmedLabel, balanceStakingLabel, balanceTotalLabel;

    private ParentController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }   

    public void init(ParentController mainController) {
        parent = mainController;
    }
    
    public void setBalance(String text) {
        balanceLabel.setText(text);
    }
    
    public void setBalanceQRLPosition() {
        balanceLabelQRL.setLayoutX(balanceLabel.getLayoutX() + balanceLabel.getWidth() + 15);
    }

    void setBalanceSpendable(String text) {
        balanceSpendableLabel.setText(text);
    }

    void setBalanceUnconfirmed(String text) {
        balanceUnconfirmedLabel.setText(text);
    }

    void setBalanceStaking(String text) {
        balanceStakingLabel.setText(text);
    }

    void setBalanceTotal(String text) {
        //Currently 'TOTAL' is set to same as Spendable
        balanceTotalLabel.setText(text);
    }
}
