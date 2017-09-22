package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Aidan
 */
public class SendController implements Initializable {

    @FXML
    AnchorPane sendPane;
    @FXML
    TextField sendField, amountField, txidArea;
    @FXML
    Label msgArea, txidLabel, msgLabel;

    private ParentController parent;

    String checkSendRegex = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void init(ParentController mainController) {
        parent = mainController;
    }

    @FXML
    private void sendButtonClicked(MouseEvent event) {
        String fromAddress = "0";
        String sendAddress = sendField.getText();
        String sendAmount = amountField.getText();

        if (sendAddress.equals("")) {
            msgArea.setVisible(true);
            if (sendAmount.equals("")) {
                msgArea.setText("Please enter an address and an amount to send.");
            } else {
                msgArea.setText("Please enter an address.");
            }
        } else if (sendAmount.equals("")) {
            msgArea.setVisible(true);
            msgArea.setText("Please enter an amount.");
        } else if (sendAddress.length() != 69 && sendAddress.charAt(0) != 'Q') {
            msgArea.setVisible(true);
            msgArea.setText("Please enter a valid address." + "Length: " + sendAddress.length() + " charat: " + sendAddress.charAt(0));
        } else if (!sendAmount.matches(checkSendRegex)) {
            msgArea.setVisible(true);
            msgArea.setText(msgArea.getText() + "\nPlease enter a valid amount to send.");
        } else {
            Task<String[]> task = parent.sendQRLTask(fromAddress, sendAddress, sendAmount);

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent taskEvent) {
                    String[] response = task.getValue();

                    txidArea.setVisible(true);
                    msgArea.setVisible(true);
                    txidLabel.setVisible(true);
                    msgLabel.setVisible(true);
                    txidArea.setText(response[1]);
                    msgArea.setText(response[3]);
                }
            });
            parent.exec.submit(task);
        }
    }
}
