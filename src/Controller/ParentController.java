package Controller;

import Model.MarketData;
import Model.NodeControl;
import Model.Transaction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
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
import javafx.scene.layout.Pane;

/**
 *
 * @author Aidan
 */
public class ParentController implements Initializable {

    //PERMANENT GUI
    @FXML
    private Label QRLVersion, nodeBox, syncBox, topQRLLabel, topQRLValue;
    @FXML
    private ImageView networkImage, syncImage;

    //SIDE MENU
    @FXML
    private JFXButton overviewButton, walletButton, sendButton, receiveButton, transactionsButton, aboutButton, exitButton;

    //OVERVIEW PANE
    @FXML
    private OverviewController overviewController;
    @FXML
    private AnchorPane overviewScreen;
    /*
    @FXML
    private AnchorPane overviewPane;
    @FXML
    private Label balanceLabel, balanceLabelQRL, balanceSpendableLabel, balanceUnconfirmedLabel, balanceStakingLabel, balanceTotalLabel;
    */
    //WALLET PANE
    @FXML
    private WalletController walletController;
    @FXML
    private AnchorPane walletScreen;

    //SEND PANE
    @FXML
    private SendController sendController;
    @FXML
    private AnchorPane sendScreen;

    //RECEIVE PANE
    @FXML
    private ReceiveController receiveController;
    @FXML
    private AnchorPane receiveScreen;

    //TRANSACTION PANE
    @FXML
    private TransactionsController transactionsController;
    @FXML
    private AnchorPane transactionsScreen;

    //ABOUT PANE
    @FXML
    private AboutController aboutController;
    @FXML
    private AnchorPane aboutScreen;

    
    private Service<Void> backgroundThread;

    public ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public NodeControl newNode;

    private AtomicInteger taskCount = new AtomicInteger(0);

    public String QRLUSDPrice = null;
    public String QRLBTCPrice = null;

    public boolean addedTransactions = false;

    static String[] suffixes = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "st"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MarketData marketData = new MarketData();
        
        try {
            FXMLLoader overviewLoader = new FXMLLoader(getClass().getResource("/View/FXMLOverview.fxml"));
            overviewScreen.getChildren().setAll((AnchorPane) overviewLoader.load());
            overviewController = overviewLoader.getController();
            overviewController.init(this);
            
            FXMLLoader walletLoader = new FXMLLoader(getClass().getResource("/View/FXMLWallet.fxml"));
            walletScreen.getChildren().setAll((AnchorPane) walletLoader.load());
            walletController = walletLoader.getController();
            walletController.init(this);
            
            FXMLLoader sendLoader = new FXMLLoader(getClass().getResource("/View/FXMLSend.fxml"));
            sendScreen.getChildren().setAll((AnchorPane) sendLoader.load());
            sendController = sendLoader.getController();
            sendController.init(this);

            FXMLLoader receiveLoader = new FXMLLoader(getClass().getResource("/View/FXMLReceive.fxml"));
            receiveScreen.getChildren().setAll((AnchorPane) receiveLoader.load());
            receiveController = receiveLoader.getController();
            receiveController.init(this);

            FXMLLoader transactionsLoader = new FXMLLoader(getClass().getResource("/View/FXMLTransactions.fxml"));
            transactionsScreen.getChildren().setAll((AnchorPane) transactionsLoader.load());
            transactionsController = transactionsLoader.getController();
            transactionsController.init(this);

            FXMLLoader aboutLoader = new FXMLLoader(getClass().getResource("/View/FXMLAbout.fxml"));
            aboutScreen.getChildren().setAll((AnchorPane) aboutLoader.load());
            aboutController = aboutLoader.getController();
            aboutController.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        changeMenuColours(overviewButton);
        changePane(overviewScreen);
        
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
            marketData.collectQRLData();
            QRLUSDPrice = marketData.getQRLUSDPrice();
            QRLBTCPrice = marketData.getQRLBTCPrice();
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

                                topQRLValue.setText("$" + Math.round((Double.parseDouble(newNode.getBalance()) * Double.parseDouble(QRLUSDPrice)) * 100.0) / 100.0);
                                
                                overviewController.setBalance(newNode.getBalance());
                                overviewController.setBalanceQRLPosition();
                                overviewController.setBalanceSpendable(newNode.getBalanceSpendable() + "   QRL");
                                overviewController.setBalanceUnconfirmed(newNode.getBalanceUnconfirmed() + "   QRL");
                                overviewController.setBalanceStaking(newNode.getBalanceStaking() + "   QRL");
                                //Currently 'TOTAL' is set to same as Spendable
                                overviewController.setBalanceTotal(newNode.getBalanceSpendable() + "   QRL");

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
                                if (newNode.getSync() == null || newNode.getBlock() == null) {
                                    overviewController.setSyncLabelVisibility(true);
                                    syncBox.setText("No QRL node detected. Please restart node and check existing connections.");
                                    if (syncImage.getImage() != errorImg) {
                                        syncImage.setImage(errorImg);
                                    }
                                } else if (newNode.getSync().equals("syncing")) {
                                    if (syncImage.getImage() != syncingImg) {
                                        syncImage.setImage(syncingImg);
                                    }
                                    overviewController.setSyncLabelVisibility(true);
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("Syncing with QRL blockchain. Downloading block #" + newNode.getBlock() + " of estimated " + newNode.getEstimatedBlocks() + ".");
                                    }
                                } else if (newNode.getSync().equals("unsynced")) {
                                    overviewController.setSyncLabelVisibility(true);
                                    if (syncImage.getImage() != errorImg) {
                                        syncImage.setImage(errorImg);
                                    }
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("Syncing with QRL blockchain. Downloading block #" + newNode.getBlock() + " of estimated 20,000.");
                                    }
                                } else if (newNode.getSync().equals("synced")) {
                                    overviewController.setSyncLabelVisibility(false);
                                    if (syncImage.getImage() != syncedImg) {
                                        syncImage.setImage(syncedImg);
                                    }
                                    if (!newNode.getBlock().equals("0")) {
                                        syncBox.setText("QRL blockchain synced. Current blockheight #" + newNode.getBlock() + ".");
                                    }
                                }
                            });
                            
                            overviewController.updateCoinbaseTX(QRLUSDPrice, QRLBTCPrice);
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

    @FXML
    private void overviewClicked(MouseEvent event) {
        manageTransactions();
        changeMenuColours(overviewButton);
        changePane(overviewScreen);
    }

    @FXML
    private void walletClicked(MouseEvent event) throws IOException {
        changeMenuColours(walletButton);
        changePane(walletScreen);
    }

    @FXML
    private void sendClicked(MouseEvent event) {
        changeMenuColours(sendButton);
        changePane(sendScreen);
    }

    @FXML
    private void receiveClicked(MouseEvent event) {
        changeMenuColours(receiveButton);
        changePane(receiveScreen);
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
                        overviewScreen.getChildren().add(dateLabel);

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
                            overviewScreen.getChildren().add(newPane);

                            layoutY += layoutBuffer + 35;
                        }
                    }

                    addedTransactions = true;
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
        changePane(transactionsScreen);
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
        changePane(aboutScreen);
    }

    @FXML
    private void exitClicked(MouseEvent event) {
        System.exit(-1);
    }

    //@Override
    public void stop() {
        exec.shutdownNow();
    }

    private Task<Void> getinfoTask() {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<Void>() {
            @Override
            public Void call() throws Exception {
                //System.out.println("Updating Info...");
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
                //System.out.println("Updating Block...");
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
                //System.out.println("Updating Transactions...");
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
                //System.out.println("Updating Wallet...");
                newNode.updateWallet();
                return null;
            }
        };
    }

    public Task<String[]> sendQRLTask(String fromAddress, String sendAddress, String sendAmount) {
        final int taskNumber = taskCount.incrementAndGet();
        return new Task<String[]>() {
            @Override
            public String[] call() throws Exception {
                //PERFORM SENDING OPERATION HERE

                String[] responses = newNode.sendQRL(fromAddress, sendAddress, sendAmount);
                return responses;
            }
        };
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
            overviewScreen.setVisible(false);
            walletScreen.setVisible(false);
            sendScreen.setVisible(false);
            receiveScreen.setVisible(false);
            transactionsScreen.setVisible(false);
            aboutScreen.setVisible(false);
            
            pane.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void setColour(JFXButton button) {
        button.setStyle("");
        button.setStyle("-fx-background-color: #303d66");
    }

    @FXML
    void resetColour(JFXButton button) {
        button.setStyle("");
        button.setStyle("-fx-background-color:  #1D2951; -fx-opacity: 50%");
    }
}