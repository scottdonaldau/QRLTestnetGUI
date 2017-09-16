/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Transaction;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
    private Label balanceLabel, balanceLabelQRL, balanceSpendableLabel,
            balanceUnconfirmedLabel, balanceStakingLabel, balanceTotalLabel,
            notSyncedLabel, weekLabel, monthLabel, yearLabel, stakeQRL, stakeBTC,
            stakeDollar;

    private ParentController parent;

    //private Label selected;
    public enum Selected {
        WEEK, MONTH, YEAR;
    }

    Selected selected;

    Transaction[] coinbaseTXs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selected = selected.WEEK;
    }

    public void init(ParentController mainController) {
        parent = mainController;
    }

    @FXML
    private void weekClicked(MouseEvent event) {
        selected = selected.WEEK;
        weekLabel.setId("selected");
        monthLabel.setId("unselected");
        yearLabel.setId("unselected");
    }

    @FXML
    private void monthClicked(MouseEvent event) {
        selected = selected.MONTH;
        weekLabel.setId("unselected");
        monthLabel.setId("selected");
        yearLabel.setId("unselected");
    }

    @FXML
    private void yearClicked(MouseEvent event) {
        selected = selected.YEAR;
        weekLabel.setId("unselected");
        monthLabel.setId("unselected");
        yearLabel.setId("selected");
    }

    //SET TEXT, POSITION, VISIBLE METHODS
    public void setBalance(String text) {
        balanceLabel.setText(text);
    }

    public void setBalanceQRLPosition() {
        balanceLabelQRL.setLayoutX(balanceLabel.getLayoutX() + balanceLabel.getWidth() + 15);
    }

    public void setBalanceSpendable(String text) {
        balanceSpendableLabel.setText(text);
    }

    public void setBalanceUnconfirmed(String text) {
        balanceUnconfirmedLabel.setText(text);
    }

    public void setBalanceStaking(String text) {
        balanceStakingLabel.setText(text);
    }

    public void setBalanceTotal(String text) {
        //Currently 'TOTAL' is set to same as Spendable
        balanceTotalLabel.setText(text);
    }

    public void setSyncLabelVisibility(Boolean visible) {
        notSyncedLabel.setVisible(visible);
    }

    //UPDATE LOCAL VARIABLES
    public void updateCoinbaseTX(String QRLUSDPrice, String QRLBTCPrice) {
        try {
            System.out.println("---------");
            System.out.println("USD Price: " + QRLUSDPrice);
            System.out.println("BTC Price: " + QRLBTCPrice);
            System.out.println("---------");
            coinbaseTXs = parent.newNode.getCoinbaseTransactions();

            double time = 0;

            double now = Instant.now().getEpochSecond();

            //Is this going to be accurate enough?
            switch (selected) {
                case WEEK:
                    time = (86400 * 7);
                    break;
                case MONTH:
                    time = (86400 * 30);
                    break;
                case YEAR:
                    time = (86400 * 365);
                    break;
            }

            double total = 0.0;

            //If the difference in time between now and when the tx happened is less
            //than the selected time, then add to total. 
            for (Transaction tx : coinbaseTXs) {
                double txTime = Double.parseDouble(tx.getTimeProperty().get());
                double txTimeDiff = now - txTime;

                if (txTimeDiff < time) {
                    total += Double.parseDouble(tx.getAmountProperty().get());
                }
            }

            //Created final values so runLater() would work. May change in future.
            final double finalTotal = total;
            final String finalQRLUSDPrice = QRLUSDPrice;
            final String finalQRLBTCPrice = QRLBTCPrice;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat dfBTC = new DecimalFormat("#.########");
                    DecimalFormat dfUSD = new DecimalFormat("#.##");
                    dfBTC.setRoundingMode(RoundingMode.CEILING);
                    dfUSD.setRoundingMode(RoundingMode.CEILING);
                    stakeQRL.setText(String.valueOf(finalTotal));
                    //Bit messy
                    stakeBTC.setText(String.valueOf(dfBTC.format(finalTotal * Double.parseDouble(finalQRLBTCPrice))));
                    stakeDollar.setText("$" + String.valueOf(dfUSD.format(finalTotal * Double.parseDouble(finalQRLUSDPrice))));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
