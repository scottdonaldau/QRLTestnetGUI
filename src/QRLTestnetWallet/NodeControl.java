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

    static String version;
    static String uptime;
    static String nodes;
    static String staking;
    static String sync;
    static Boolean connected = false;

    public void setup() {
        node = new ContactNode();
    }

    public JsonObject sendCommandObject(String command, int timeout) {
        JsonObject objResponse = node.sendCommandAndGetJsonObject(command, timeout);
        return objResponse;
    }

    public JsonArray sendCommandArray(String command, String nested, int timeout) {
        JsonArray arrayResponse = node.sendCommandAndGetJsonArray(command, nested, timeout);
        return arrayResponse;
    }

    public void updateInfo() {
        JsonObject updateResponse = sendCommandObject("json getinfo", 50);

        if (updateResponse != null) {
            //String test = updateResponse.asString();
            // System.out.println("WHOLE THING: " + test);

            version = updateResponse.getString("version", "Unavailable");
            uptime = updateResponse.getString("uptime", "Unavailable");
            nodes = updateResponse.getString("nodes_connected", "Unavailable");
            staking = updateResponse.getString("staking_status", "Unavailable");
            sync = updateResponse.getString("sync_status", "Unavailable");
        } else {
            System.out.println("UPDATE RESPONSE IS NULL");
        }
    }

    //Update to allow for multiple addresses
    public void updateWallet(String nested) {
        JsonArray arrayResponse = sendCommandArray("json wallet", nested, 50);
        if (arrayResponse != null) {
            JsonObject firstAddress = arrayResponse.get(0).asObject();

            address = firstAddress.getString("address", "Unavailable");
            balance = firstAddress.getString("balance", "Unavailable");
        } else {
            //if(address = )
            address = "Unavailable";
            balance = "Unavailable";
        }
    }

    public void updateWallet() {
        JsonArray arrayResponse = sendCommandArray("json wallet", "list_addresses", 50);
        if (arrayResponse != null) {
            JsonObject firstAddress = arrayResponse.get(0).asObject();

            address = firstAddress.getString("address", "Unavailable");
            balance = firstAddress.getString("balance", "Unavailable");
        } else {

        }
    }

    public String[] sendQRL(String fromAddress, String toAddress, String amount) {
        
        String query = "json send " + fromAddress + " " + toAddress + " " + amount;
        JsonObject sendResponse = sendCommandObject(query, 1000);

        String stringResponse = sendResponse.getString("message", "Undelivered...");
        System.out.println("SERVER THING LOOK AT ME: " + stringResponse);
        String[] responses = stringResponse.split(">>>");
        //System.out.println();
        
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
    
    public boolean getConnected() {
        return connected;
    }
}
