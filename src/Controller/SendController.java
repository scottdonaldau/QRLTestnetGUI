package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    private ParentController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }   

    public void init(ParentController mainController) {
        parent = mainController;
    }
    
    // FUNCTION FROM PREVIOUS IMPLEMENTATION
    // NEED TO RE-IMPLEMENT THIS
    /*
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
            Task<Void> task = sendQRLTask();

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent taskEvent) {
                    //Add here
                }
            });

            exec.submit(task);

        }
    }
*/
}