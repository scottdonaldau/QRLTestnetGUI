// Put in license header?
package QRLTestnetWallet;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import static QRLTestnetWallet.ContactQRL.getAddress;
import static QRLTestnetWallet.ContactQRL.getBalance;
import static QRLTestnetWallet.ContactQRL.getVersion;
import static QRLTestnetWallet.ContactQRL.getUptime;
import static QRLTestnetWallet.ContactQRL.getNodes;
import static QRLTestnetWallet.ContactQRL.getStaking;
import static QRLTestnetWallet.ContactQRL.getSync;
import static QRLTestnetWallet.ContactQRL.sendQRL;

/**
 *
 * @author Aidan
 */
public class FXMLDocumentController implements Initializable {

    //PERMANENT GUI
    @FXML
    private ImageView btn_exit;
    
    //SIDE MENU
    @FXML
    private JFXButton walletButton, sendButton, transactionsButton, aboutButton, exitButton;
    
    //WALLET PANE
    @FXML
    private AnchorPane walletPane;
    @FXML
    private TextField walletAddress;
    @FXML
    private Label walletBalance, versionLabel, uptimeLabel, nodesLabel, stakingLabel, syncLabel;

    //SEND PANE
    @FXML
    private AnchorPane sendPane;
    @FXML
    private JFXTextField sendField, amountField;
    @FXML
    private TextField txidArea;
    @FXML
    private Label msgLabel,txidLabel, msgArea;
    
    //TRANSACTION PANE
    @FXML
    private AnchorPane transactionPane;
    
    //ABOUT PANE
    @FXML
    private AnchorPane aboutPane;

    @FXML
    private void handleButtonAction(MouseEvent event) {
        System.exit(-1);
    }

    @FXML
    private void handleButtonAction2(MouseEvent event) {
        btn_exit.setImage(new Image("closePressed.png"));
    }

    @FXML
    private void handleButtonAction3(MouseEvent event) {
        btn_exit.setImage(new Image("exit.png"));
    }

    //This will need to be updated automatically instead of only a the beginning
    //of the program.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletAddress.setText(getAddress());
        walletBalance.setText(getBalance());

        versionLabel.setText(getVersion());
        uptimeLabel.setText(getUptime());
        nodesLabel.setText(getNodes());
        stakingLabel.setText(getStaking());
        syncLabel.setText(getSync());
    }

    @FXML
    private void walletClicked(MouseEvent event) {
        changeMenuColours(walletButton);
        changePane(walletPane);
    }

    @FXML
    private void sendClicked(MouseEvent event) {
        changeMenuColours(sendButton);
        changePane(sendPane);
    }

    @FXML
    private void transactionsClicked(MouseEvent event) {
        changeMenuColours(transactionsButton);
        changePane(transactionPane);
    }

    @FXML
    private void aboutClicked(MouseEvent event) {
        changeMenuColours(aboutButton);
        changePane(aboutPane);
    }

    @FXML
    private void exitClicked(MouseEvent event) {
        System.exit(-1);
    }

    @FXML
    private void sendButtonClicked(MouseEvent event) {
        String fromAddress = "0";
        String sendAddress = sendField.getText();
        String sendAmount = amountField.getText();
        String[] responses = sendQRL(fromAddress, sendAddress, sendAmount);

        txidArea.setVisible(true);
        msgArea.setVisible(true);
        txidLabel.setVisible(true);
        msgLabel.setVisible(true);
        txidArea.setText(responses[1]);
        msgArea.setText(responses[3]);
    }
    
    @FXML 
    void changeMenuColours(JFXButton button) {
        resetColour(walletButton);
        resetColour(sendButton);
        resetColour(transactionsButton);
        resetColour(aboutButton);
        resetColour(exitButton);
        setColour(button);
    }
    
    @FXML
    void changePane(AnchorPane pane) {
        walletPane.setVisible(false);
        sendPane.setVisible(false);
        transactionPane.setVisible(false);
        aboutPane.setVisible(false);
        pane.setVisible(true);
    }

    @FXML
    void setColour(JFXButton button) {
        button.setStyle("");
        button.setStyle("-fx-background-color: #607685");
    }

    @FXML
    void resetColour(JFXButton button) {
        button.setStyle("");
        button.setStyle("-fx-background-color: #3A4750");
    }
}