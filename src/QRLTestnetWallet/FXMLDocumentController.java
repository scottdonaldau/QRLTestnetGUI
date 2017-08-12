/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    @FXML
    private ImageView btn_exit;
    @FXML
    private JFXButton walletButton;
    @FXML
    private JFXButton sendButton;
    @FXML
    private JFXButton transactionsButton;
    @FXML
    private JFXButton aboutButton;
    @FXML
    private JFXButton exitButton;
    @FXML
    private TextField walletAddress;
    @FXML
    private TextField txidArea;
    @FXML
    private Label msgArea;
    @FXML
    private JFXTextField sendField;
    @FXML
    private JFXTextField amountField;
    @FXML
    private Label walletBalance;
    @FXML
    private Label versionLabel;
    @FXML
    private Label uptimeLabel;
    @FXML
    private Label nodesLabel;
    @FXML
    private Label stakingLabel;
    @FXML
    private Label syncLabel;
    @FXML
    private Label sendResponse;
    @FXML
    private Label txidLabel;
    @FXML
    private Label msgLabel;
    @FXML
    private AnchorPane walletPane;
    @FXML
    private AnchorPane sendPane;
    @FXML
    private AnchorPane transactionPane;
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
        btn_exit.setImage(new Image("close.png"));
    }

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
        setColour(walletButton);
        resetColour(sendButton);
        resetColour(transactionsButton);
        resetColour(aboutButton);
        resetColour(exitButton);
        walletPane.setVisible(true);
        sendPane.setVisible(false);
        transactionPane.setVisible(false);
        aboutPane.setVisible(false);
    }

    @FXML
    private void sendClicked(MouseEvent event) {
        resetColour(walletButton);
        setColour(sendButton);
        resetColour(transactionsButton);
        resetColour(aboutButton);
        resetColour(exitButton);
        walletPane.setVisible(false);
        sendPane.setVisible(true);
        transactionPane.setVisible(false);
        aboutPane.setVisible(false);
    }

    @FXML
    private void transactionsClicked(MouseEvent event) {
        resetColour(walletButton);
        resetColour(sendButton);
        setColour(transactionsButton);
        resetColour(aboutButton);
        resetColour(exitButton);
        walletPane.setVisible(false);
        sendPane.setVisible(false);
        transactionPane.setVisible(true);
        aboutPane.setVisible(false);
    }

    @FXML
    private void aboutClicked(MouseEvent event) {
        resetColour(walletButton);
        resetColour(sendButton);
        resetColour(transactionsButton);
        setColour(aboutButton);
        resetColour(exitButton);
        walletPane.setVisible(false);
        sendPane.setVisible(false);
        transactionPane.setVisible(false);
        aboutPane.setVisible(true);
    }

    @FXML
    private void exitClicked(MouseEvent event) {
        resetColour(walletButton);
        resetColour(sendButton);
        resetColour(transactionsButton);
        resetColour(aboutButton);
        setColour(exitButton);
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