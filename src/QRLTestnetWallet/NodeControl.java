/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QRLTestnetWallet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Aidan
 */
public class NodeControl {

    private ContactNode node;
    static String hostName = "127.0.0.1";
    static int portNumber = 2000;

    static String address;

    static String balance;
    static String balanceSpendable;
    static String balanceStaking;
    static String balanceUnconfirmed;

    public Transaction[] transactions = null;
    public Transaction[] recentTransactions = null;
    public Transaction[] coinbaseTransactions = null;

    public int estimatedBlocks;
    public String block;

    static String version;
    static String uptime;
    static String nodes;
    static String staking;
    static String sync;
    static Boolean connected = false;
    
    Double totalWinnings;

    public void setup() {
        node = new ContactNode();
    }

    public JsonObject sendCommandObject(String command, int timeout) {
        JsonObject objResponse = node.sendCommandAndGetJsonObject(command, timeout);
        return objResponse;
    }

    public JsonObject sendCommandObject(String command, String nested, int timeout) {
        JsonObject objResponse = node.sendCommandAndGetJsonObject(command, nested, timeout);
        return objResponse;
    }

    public JsonArray sendCommandArray(String command, String nested, int timeout) {
        JsonArray arrayResponse = node.sendCommandAndGetJsonArray(command, nested, timeout);
        return arrayResponse;
    }

    public void updateInfo() {
        JsonObject updateResponse = sendCommandObject("json getinfo", 50);

        if (updateResponse != null) {
            version = updateResponse.getString("version", "Unavailable");
            uptime = updateResponse.getString("uptime", "Unavailable");
            nodes = updateResponse.getString("nodes_connected", "Unavailable");
            staking = updateResponse.getString("staking_status", "Unavailable");
            sync = updateResponse.getString("sync_status", "Unavailable");
        } else {
            System.out.println("Error updating info...");
        }
    }

    //Update to allow for multiple addresses
    public void updateWallet(String nested) {
        JsonArray arrayResponse = sendCommandArray("json wallet", nested, 50);
        if (arrayResponse != null) {
            JsonObject firstAddress = arrayResponse.get(0).asObject();

            String balanceResponse = firstAddress.getString("balance", "Unavailable");
            String parts[] = balanceResponse.split("\\(");
            balance = parts[1];

            address = firstAddress.getString("address", "Unavailable");
            //balance = firstAddress.getString("balance", "Unavailable");
        } else {
            address = "Unavailable";
            balance = "Unavailable";
        }
    }

    public void updateWallet() {
        JsonArray arrayResponse = sendCommandArray("json wallet", "list_addresses", 50);
        if (arrayResponse != null) {
            JsonObject firstAddress = arrayResponse.get(0).asObject();

            String balanceResponse = firstAddress.getString("balance", "Unavailable");
            String parts[] = balanceResponse.split("\\(");
            balance = parts[0];
            balanceSpendable = parts[1].substring(0, parts[1].length() - 1);
            balanceStaking = parts[0];

            double difference = Double.parseDouble(balanceSpendable) - Double.parseDouble(balance);

            if (difference > 0.0) {
                balanceUnconfirmed = Double.toString(difference);
            } else {
                balanceUnconfirmed = "0.00";
            }

            address = firstAddress.getString("address", "Unavailable");
        } else {

        }
    }

    public void updateBlock() {
        JsonObject updateResponse = sendCommandObject("json_block", "blockheader", 50);

        if (updateResponse != null) {
            block = Integer.toString(updateResponse.getInt("blocknumber", 0));
            System.out.println("BLOCK: " + block);
            try {
                Double timestamp = updateResponse.getDouble("timestamp", 0) * 1000;
                Double now = (double) Instant.now().toEpochMilli();
                Double difference = now - timestamp;
                estimatedBlocks = (int) ((difference / 46000) + Integer.valueOf(block));
            } catch (Exception e) {
                System.out.println("Error retrieving block height and timestamp...");
                e.printStackTrace();
            }
        } else {
            System.out.println("Error updating blocks...");
        }
    }

    public void updateTransactions() {
        JsonArray arrayResponse = sendCommandArray("json search " + address, "transactions", 200);

        if (arrayResponse != null) {
            ArrayList<Transaction> listTransactions = new ArrayList<Transaction>();
            ArrayList<Transaction> listRecentTransactions = new ArrayList<Transaction>();
            ArrayList<Transaction> listCoinbaseTransactions = new ArrayList<Transaction>();
            for (JsonValue value : arrayResponse) {
                JsonObject object = value.asObject();
                try {
                    Transaction transaction = new Transaction(
                            Double.toString(object.getDouble("timestamp", -1.0)), 
                            object.getString("subtype", "default"),
                            object.getString("txhash", "default"),
                            object.getString("txto", "default"),
                            object.getString("txfrom", "default"),
                            Double.toString(object.getDouble("amount", -1.0)),
                            Integer.toString(object.getInt("block", -1))
                    );

                    if(transaction.getTypeProperty().get().equals("TX")) {
                        System.out.println("TX = TX");
                        listTransactions.add(transaction);
                    } else if(transaction.getTypeProperty().get().equals("COINBASE")) {
                        System.out.println("TX = COINBASE");
                        listCoinbaseTransactions.add(transaction);
                    } else {
                        System.out.println("NOTHING HERE");
                    }

                } catch (Exception e) {
                    System.out.println("Error managing transactions...");
                    e.printStackTrace();
                }
            }
            
            Transaction[] transactionArray = listTransactions.toArray(new Transaction[listTransactions.size()]);
            
            Transaction[] recentTransactionArray = listRecentTransactions.toArray(new Transaction[listTransactions.size()]);
            Transaction[] coinbaseTransactionArray = listCoinbaseTransactions.toArray(new Transaction[listTransactions.size()]);
            
            
            transactions = transactionArray;
            recentTransactions = recentTransactionArray;
            coinbaseTransactions = coinbaseTransactionArray;
            addStakeWinnings();
        } else {
            System.out.println("Error updating transactions...");
        }
    }
    
    public void addStakeWinnings() {
        totalWinnings = 0.0;
        
        for(Transaction t : coinbaseTransactions) {
            totalWinnings += Double.parseDouble(t.getAmountProperty().get());
        }
    }

    public String[] sendQRL(String fromAddress, String toAddress, String amount) {

        String query = "json send " + fromAddress + " " + toAddress + " " + amount;
        JsonObject sendResponse = sendCommandObject(query, 1000);

        String stringResponse = sendResponse.getString("message", "Undelivered...");
        String[] responses = stringResponse.split(">>>");

        return responses;
    }

    public void connect() {
        try {
            System.out.println("About to connect!!");
            node.connect();
            connected = true;
        } catch (Exception e) {
            System.out.println("EEEEERRRROR");
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public String getUptime() {
        if (uptime != null) {
            try {
                double timed = Double.parseDouble(uptime);
                int time = (int) timed;

                int hours = time / 3600;
                int secondsLeft = time - hours * 3600;
                int minutes = secondsLeft / 60;
                int seconds = secondsLeft - minutes * 60;

                String formattedTime = "";

                if (hours != 0) {
                    formattedTime += hours + " hrs ";
                }

                if (minutes != 0) {
                    formattedTime += minutes + " mins ";
                }

                if (seconds != 0) {
                    formattedTime += seconds + " seconds";
                }
                return formattedTime;
            } catch (Exception e) {
                return uptime;
            }
        } else {
            return "";
        }
    }

    public int getEstimatedBlocks() {
        return estimatedBlocks;
    }

    public String getBlock() {
        return block;
    }

    public String getNodes() {
        return nodes;
    }

    public String getStaking() {
        return staking;
    }

    public String getSync() {
        return sync;
    }

    public String getAddress() {
        return address;
    }

    public String getBalance() {
        return balance;
    }

    public String getBalanceSpendable() {
        return balanceSpendable;
    }

    public String getBalanceUnconfirmed() {
        return balanceUnconfirmed;
    }

    public String getBalanceStaking() {
        return balanceStaking;
    }

    public boolean getConnected() {
        return connected;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }
    
    public Transaction[] getCoinbaseTransactions() {
        return coinbaseTransactions;
    }
}
