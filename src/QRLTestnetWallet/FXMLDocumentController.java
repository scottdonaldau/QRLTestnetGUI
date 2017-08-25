// Put in license header?
package QRLTestnetWallet;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

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
    private Label msgLabel, txidLabel, msgArea;

    //TRANSACTION PANE
    @FXML
    private AnchorPane transactionPane;
    @FXML
    private JFXTreeTableView txTable;

    //ABOUT PANE
    @FXML
    private AnchorPane aboutPane;
    private Service<Void> backgroundThread;

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

    public NodeControl newNode;

    private AtomicInteger taskCount = new AtomicInteger(0);

    String checkSendRegex = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        newNode = new NodeControl();
        newNode.setup();
        while (!newNode.getConnected()) {
            newNode.connect();
        }

        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (true) {
                            //IntegerProperty pendingTasks = new SimpleIntegerProperty(0);

                            Task<Void> infoTask = getinfoTask();
                            Task<Void> walletTask = getWalletTask();

                            //infoTask.setOnSucceeded(taskEvent -> pendingTasks.set(pendingTasks.get() - 1));
                            exec.submit(infoTask);
                            exec.submit(walletTask);
                            Thread.sleep(500);
                            Platform.runLater(() -> {
                                versionLabel.setText(newNode.getVersion());
                                uptimeLabel.setText(newNode.getUptime());
                                nodesLabel.setText(newNode.getNodes());
                                stakingLabel.setText(newNode.getStaking());
                                syncLabel.setText(newNode.getSync());
                                walletBalance.setText(newNode.getBalance());

                                if (walletAddress.getText() == null || walletAddress.getText() == "Unavailable" || walletAddress.getText().equals("")) {
                                    walletAddress.setText(newNode.getAddress());
                                }
                            });
                            Thread.sleep(500);

                        }
                    }
                };
            }
        };

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("done");
                uptimeLabel.textProperty().unbind();
            }
        });
        backgroundThread.restart();

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

        JFXTreeTableColumn txTime = new JFXTreeTableColumn("Time");
        JFXTreeTableColumn txHash = new JFXTreeTableColumn("Tx Hash");
        JFXTreeTableColumn txTo = new JFXTreeTableColumn("To");
        JFXTreeTableColumn txAmount = new JFXTreeTableColumn("Amount");
        JFXTreeTableColumn txBlock = new JFXTreeTableColumn("Block");

        Task<String[][]> transactionTask = getTransactionsTask();

        transactionTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent taskEvent) {
                try {
                    String[][] transactions = transactionTask.getValue();

                    ObservableList<Transaction> txList = FXCollections.observableArrayList();
                    for (String[] s : transactions) {
                        Transaction tempTX = new Transaction(s[0], s[1], s[2], s[3], s[4]);
                        txList.add(tempTX);
                    }

                    JFXTreeTableColumn<Transaction, String> time = new JFXTreeTableColumn<>("Time");
                    time.setPrefWidth(150);
                    time.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Transaction, String> param) {
                            return param.getValue().getValue().timeProperty;
                        }
                    });

                    JFXTreeTableColumn<Transaction, String> txHash = new JFXTreeTableColumn<>("TX Hash");
                    txHash.setPrefWidth(150);
                    txHash.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Transaction, String> param) {
                            return param.getValue().getValue().hashProperty;
                        }
                    });

                    JFXTreeTableColumn<Transaction, String> to = new JFXTreeTableColumn<>("To");
                    to.setPrefWidth(150);
                    to.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Transaction, String> param) {
                            return param.getValue().getValue().toProperty;
                        }
                    });

                    JFXTreeTableColumn<Transaction, String> amount = new JFXTreeTableColumn<>("Amount");
                    amount.setPrefWidth(75);
                    amount.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Transaction, String> param) {
                            return param.getValue().getValue().amountProperty;
                        }
                    });

                    JFXTreeTableColumn<Transaction, String> block = new JFXTreeTableColumn<>("Block");
                    block.setPrefWidth(50);
                    block.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Transaction, String> param) {
                            return param.getValue().getValue().blockProperty;
                        }
                    });

                    final TreeItem<Transaction> root = new RecursiveTreeItem<Transaction>(txList, RecursiveTreeObject::getChildren);
                    txTable.getColumns().setAll(time, txHash, to, amount, block);
                    txTable.setRoot(root);
                    txTable.setShowRoot(false);

                } catch (Exception e) {
                    System.out.println("Issue displaying transactions list...");
                    e.printStackTrace();
                }
            }
        });

        exec.submit(transactionTask);
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

    private ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    //@Override
    public void stop() {
        exec.shutdownNow();
    }

    private Task<Void> getinfoTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Void>() {
            @Override
            public Void call() throws Exception {
                System.out.println("Updating Info...");
                newNode.updateInfo();
                return null;
            }
        };
    }

    private Task<String[][]> getTransactionsTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<String[][]>() {
            @Override
            public String[][] call() throws Exception {
                System.out.println("Updating Transactions...");
                newNode.updateTransactions();

                String[][] transactions = newNode.getTransactions();

                return transactions;
            }
        };
    }

    private Task<Void> getWalletTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Void>() {
            @Override
            public Void call() throws Exception {
                System.out.println("Updating Wallet...");
                newNode.updateWallet();
                return null;
            }
        };
    }

    private Task<Void> sendQRLTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Void>() {
            @Override
            public Void call() throws Exception {
                String fromAddress = "0";
                String sendAddress = sendField.getText();
                String sendAmount = amountField.getText();
                /*
                System.out.println("DOING WHAAAT");
                for (int count = 1; count <= 5; count++) {
                    Thread.sleep(1000);
                    updateMessage("Task " + taskNumber + ": Count " + count);
                }
                 */
                String[] responses = newNode.sendQRL(fromAddress, sendAddress, sendAmount);
                Platform.runLater(() -> {
                    txidArea.setVisible(true);
                    msgArea.setVisible(true);
                    txidLabel.setVisible(true);
                    msgLabel.setVisible(true);
                    txidArea.setText(responses[1]);
                    msgArea.setText(responses[3]);
                });

                return null;
            }
        };
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
        button.setStyle("-fx-background-color: #4a5474");
    }

    @FXML
    void resetColour(JFXButton button) {
        button.setStyle("");
        button.setStyle("-fx-background-color:  #1D2951");
    }
}
