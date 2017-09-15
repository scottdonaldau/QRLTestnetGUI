package Controller;

import Model.MarketData;
import Model.NodeControl;
import Model.Transaction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 *
 * @author Aidan
 */
public class ParentController implements Initializable {

    //PERMANENT GUI
    @FXML
    private ImageView btn_exit;
    @FXML
    private Label QRLVersion, nodeBox, syncBox, notSyncedLabel, topQRLLabel, topQRLValue;
    @FXML
    private ImageView networkImage, syncImage;

    //SIDE MENU
    @FXML
    private JFXButton overviewButton, walletButton, sendButton, receiveButton, transactionsButton, aboutButton, exitButton;

    //OVERVIEW PANE
    @FXML
    private AnchorPane overviewPane;
    @FXML
    private Label balanceLabel, balanceLabelQRL, balanceSpendableLabel, balanceUnconfirmedLabel, balanceStakingLabel, balanceTotalLabel;
    //@FXML
    //private JFXTreeTableView recentTransactionsTable;

    //WALLET PANE
    @FXML
    private WalletController walletController;
    @FXML
    private AnchorPane walletScreen;
    //@FXML
    //private AnchorPane walletPane;
    //@FXML
    //private TextField walletAddress;
    //@FXML
    //private Label walletBalance, versionLabel, uptimeLabel, nodesLabel, stakingLabel, syncLabel;

    //SEND PANE
    @FXML
    private AnchorPane sendPane;
    @FXML
    private JFXTextField sendField, amountField;
    @FXML
    private TextField txidArea;
    @FXML
    private Label msgLabel, txidLabel, msgArea;

    //RECEIVE PANE
    @FXML
    private AnchorPane receivePane;

    //TRANSACTION PANE
    @FXML
    private AnchorPane transactionPane;
    //@FXML
    //private JFXTreeTableView txTable;

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
        btn_exit.setImage(new Image("images/closePressed.png"));
    }

    @FXML
    private void handleButtonAction3(MouseEvent event) {
        btn_exit.setImage(new Image("images/close.png"));
    }

    @FXML
    private void displayNodesMessage(MouseEvent event) {
        nodeBox.setVisible(true);
    }

    @FXML
    private void hideNodesMessage(MouseEvent event) {
        nodeBox.setVisible(false);
    }

    @FXML
    private void displaySyncMessage(MouseEvent event) {
        syncBox.setVisible(true);
    }

    @FXML
    private void hideSyncMessage(MouseEvent event) {
        syncBox.setVisible(false);
    }

    public NodeControl newNode;

    private AtomicInteger taskCount = new AtomicInteger(0);

    String checkSendRegex = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";

    public String QRLPrice = null;

    public boolean addedTransactions = false;

    static String[] suffixes = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "st"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            //walletPane.getChildren().setAll(FXMLLoader.load("vista2.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXMLWallet.fxml"));
            walletScreen.getChildren().setAll((AnchorPane) loader.load());
            
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXMLWallet.fxml"));
            //Parent root = (Parent) loader.load();
            walletController = loader.getController();
            System.out.println("LOOK AT ME: " + walletController);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("View/FXMLWallet.fxml"));
        walletController = fxmlLoader.<WalletController>getController();
        //walletController.init(this);
        
         */
        System.out.println("THING");
        Image syncingImg = new Image("images/loading.gif");
        Image errorImg = new Image("images/error.png");
        Image syncedImg = new Image("images/synced.png");
        syncImage.setImage(errorImg);

        newNode = new NodeControl();
        newNode.setup();
        while (!newNode.getConnected()) {
            newNode.connect();
        }
        try {
            QRLPrice = MarketData.collectQRLData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        manageTransactions();

        //String response = "";
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
                            Task<Void> blockTask = getBlockTask();

                            //infoTask.setOnSucceeded(taskEvent -> pendingTasks.set(pendingTasks.get() - 1));
                            exec.submit(infoTask);
                            exec.submit(walletTask);
                            exec.submit(blockTask);

                            if (!addedTransactions) {
                                manageTransactions();
                            }

                            Thread.sleep(500);
                            Platform.runLater(() -> {

                                topQRLValue.setText("$" + Math.round((Double.parseDouble(newNode.getBalance()) * Double.parseDouble(QRLPrice)) * 100.0) / 100.0);

                                balanceLabel.setText(newNode.getBalance());

                                balanceLabelQRL.setLayoutX(balanceLabel.getLayoutX() + balanceLabel.getWidth() + 15);

                                balanceSpendableLabel.setText(newNode.getBalanceSpendable() + "   QRL");
                                balanceUnconfirmedLabel.setText(newNode.getBalanceUnconfirmed() + "   QRL");
                                balanceStakingLabel.setText(newNode.getBalanceStaking() + "   QRL");
                                balanceTotalLabel.setText(newNode.getBalanceSpendable() + "   QRL");
                                topQRLLabel.setText(newNode.getBalanceSpendable() + "  QRL");

                                if (QRLVersion.getText().equals("") && newNode.getVersion() != null) {
                                    QRLVersion.setText("Quantum Resistant Ledger " + newNode.getVersion());
                                }

                                if (newNode.getNodes() != null) {
                                    int nodes = Integer.parseInt(newNode.getNodes());
                                    if (nodes < 3) {
                                        networkImage.setImage(new Image("images/network1.png"));
                                    } else if (nodes < 5) {
                                        networkImage.setImage(new Image("images/network2.png"));
                                    } else if (nodes < 7) {
                                        networkImage.setImage(new Image("images/network3.png"));
                                    } else if (nodes >= 7) {
                                        networkImage.setImage(new Image("images/network4.png"));
                                    }

                                    nodeBox.setText(newNode.getNodes() + " nodes connected.");
                                }
                                System.out.println("SYNC STATUS: " + newNode.getSync());
                                if (newNode.getSync() == null || newNode.getBlock() == null) {
                                    notSyncedLabel.setVisible(true);
                                    syncBox.setText("No QRL node detected. Please restart node and check existing connections.");
                                    if (syncImage.getImage() != errorImg) {
                                        syncImage.setImage(errorImg);
                                    }
                                } else if (newNode.getSync().equals("syncing")) {
                                    if (syncImage.getImage() != syncingImg) {
                                        syncImage.setImage(syncingImg);
                                    }
                                    notSyncedLabel.setVisible(true);
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("Syncing with QRL blockchain. Downloading block #" + newNode.getBlock() + " of estimated " + newNode.getEstimatedBlocks() + ".");
                                    }
                                } else if (newNode.getSync().equals("unsynced")) {
                                    notSyncedLabel.setVisible(true);
                                    if (syncImage.getImage() != errorImg) {
                                        syncImage.setImage(errorImg);
                                    }
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("Syncing with QRL blockchain. Downloading block #" + newNode.getBlock() + " of estimated 20,000.");
                                    }
                                } else if (newNode.getSync().equals("synced")) {
                                    notSyncedLabel.setVisible(false);
                                    if (syncImage.getImage() != syncedImg) {
                                        syncImage.setImage(syncedImg);
                                    }
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("QRL blockchain synced. Current blockheight #" + newNode.getBlock() + ".");
                                    }
                                }

                                /*
                                uptimeLabel.setText(newNode.getUptime());
                                nodesLabel.setText(newNode.getNodes());
                                stakingLabel.setText(newNode.getStaking());
                                syncLabel.setText(newNode.getSync());
                                walletBalance.setText(newNode.getBalance());
                                 */
 /*
                                if (walletAddress.getText() == null || walletAddress.getText() == "Unavailable" || walletAddress.getText().equals("")) {
                                    walletAddress.setText(newNode.getAddress());
                                }
                                 */
                            });
                            Thread.sleep(2000);

                        }
                    }
                };
            }
        };
        
        /*
        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("done");
                uptimeLabel.textProperty().unbind();
            }
        });
         */
        backgroundThread.restart();

    }

    @FXML
    private void overviewClicked(MouseEvent event) {
        manageTransactions();
        changeMenuColours(overviewButton);
        changePane(overviewPane);
    }

    @FXML
    private void walletClicked(MouseEvent event) throws IOException {
        changeMenuColours(walletButton);
        changePane(walletScreen);
    }

    @FXML
    private void sendClicked(MouseEvent event) {
        changeMenuColours(sendButton);
        changePane(sendPane);
    }

    @FXML
    private void receiveClicked(MouseEvent event) {
        changeMenuColours(receiveButton);
        changePane(receivePane);
    }

    private void manageTransactions() {
        Task<Transaction[]> transactionTask = getTransactionsTask();

        transactionTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent taskEvent) {
                try {
                    Transaction[] transactions = transactionTask.getValue();

                    List<Transaction> txList = new ArrayList();

                    for (Transaction t : transactions) {
                        txList.add(t);
                    }

                    Collections.sort(txList, new Comparator<Transaction>() {
                        public int compare(Transaction o1, Transaction o2) {
                            return o2.getTimeProperty().get().compareTo(o1.getTimeProperty().get());
                        }
                    });

                    if (txList.size() > 4) {
                        txList.subList(4, txList.size()).clear();
                    }

                    Set<String> timeSet = new HashSet<>();

                    Map<String, List<Transaction>> timeMap = new HashMap<String, List<Transaction>>();

                    for (Transaction t : txList) {
                        List<Transaction> newTXList = new ArrayList<Transaction>();

                        Double newDbl = Double.parseDouble(t.getTimeProperty().get()) * 1000;

                        Date date = new Date(newDbl.longValue());
                        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.US);
                        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.US);
                        int day = Integer.parseInt(dayFormat.format(date));
                        String monthDayStr = monthFormat.format(date).toUpperCase() + " " + day + suffixes[day];

                        if (timeMap.containsKey(monthDayStr)) {
                            List<Transaction> tempTXList = timeMap.get(monthDayStr);
                            tempTXList.add(t);
                            timeMap.put(monthDayStr, tempTXList);
                        } else {
                            newTXList.add(t);
                            timeMap.put(monthDayStr, newTXList);
                        }
                    }

                    Integer labelLayoutX = 14;
                    Integer paneLayoutX = 30;
                    Integer layoutY = 177;
                    Integer layoutBuffer = 27;

                    for (Map.Entry<String, List<Transaction>> entry : timeMap.entrySet()) {
                        Label dateLabel = new Label(entry.getKey());
                        dateLabel.setId("dateLabel");
                        dateLabel.setLayoutX(labelLayoutX);
                        dateLabel.setLayoutY(layoutY);
                        layoutY += layoutBuffer;
                        dateLabel.setVisible(true);
                        overviewPane.getChildren().add(dateLabel);

                        System.out.println("HASHMAP KEY: " + entry.getKey());

                        List<Transaction> tempList = entry.getValue();

                        for (Transaction t : tempList) {
                            Pane newPane = new Pane();
                            newPane.setId("txPane");
                            newPane.setLayoutX(paneLayoutX);
                            newPane.setLayoutY(layoutY);
                            newPane.setPrefSize(500, 53);

                            Double newDbl = Double.parseDouble(t.getTimeProperty().get()) * 1000;
                            Date date = new Date(newDbl.longValue());
                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.US);
                            String timeStr = timeFormat.format(date);

                            Label time = new Label(timeStr);
                            time.setId("timeLabel");
                            TextField address = new TextField();
                            address.setId("addressLabel");
                            address.setPrefWidth(240);
                            Label amount = new Label();

                            if (t.getToProperty().get().equals(newNode.getAddress())) {
                                address.setText(t.getFromProperty().get());
                                amount.setText("+" + t.getAmountProperty().get() + " QRL");
                                amount.setId("inputQRLLabel");
                            } else {
                                address.setText(t.getToProperty().get());
                                amount.setText("-" + t.getAmountProperty().get() + " QRL");
                                amount.setId("outputQRLLabel");
                            }

                            time.setLayoutX(14);
                            time.setLayoutY(16);
                            address.setLayoutX(123);
                            address.setLayoutY(16);
                            amount.setLayoutX(375);
                            amount.setLayoutY(16);

                            newPane.getChildren().addAll(time, address, amount);
                            newPane.setVisible(true);
                            overviewPane.getChildren().add(newPane);

                            System.out.println(t.getTimeProperty().get());
                            layoutY += layoutBuffer + 35;
                        }
                    }

                    addedTransactions = true;
                    System.out.println("TRANSACTIONS HAVE BEEN DISPLAYED!!!");
                } catch (Exception e) {
                    System.out.println("Issue displaying transactions list...");
                    e.printStackTrace();
                }
            }
        });

        exec.submit(transactionTask);
    }

    @FXML

    private void transactionsClicked(MouseEvent event) {
        changeMenuColours(transactionsButton);
        changePane(transactionPane);
        /*
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
         */
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

    private Task<Void> getBlockTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Void>() {
            @Override
            public Void call() throws Exception {
                System.out.println("Updating Block...");
                newNode.updateBlock();
                return null;
            }
        };
    }

    private Task<Transaction[]> getTransactionsTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Transaction[]>() {
            @Override
            public Transaction[] call() throws Exception {
                System.out.println("Updating Transactions...");
                newNode.updateTransactions();
                //String[][] transactions = null;
                //transactions = newNode.getTransactions();

                Transaction[] transactions = null;
                transactions = newNode.getTransactions();
                if (transactions == null) {
                    return null;
                } else {
                    return transactions;
                }
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
        resetColour(overviewButton);
        resetColour(walletButton);
        resetColour(sendButton);
        resetColour(receiveButton);
        resetColour(transactionsButton);
        resetColour(aboutButton);
        resetColour(exitButton);
        setColour(button);
    }

    @FXML
    void changePane(AnchorPane pane) {
        try {

            overviewPane.setVisible(false);
            sendPane.setVisible(false);
            receivePane.setVisible(false);
            transactionPane.setVisible(false);
            aboutPane.setVisible(false);
            walletScreen.setVisible(false);
            walletController.setVisibility(true);
            pane.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
