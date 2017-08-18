package QRLTestnetWallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ContactQRL {

    static String hostName = "127.0.0.1";
    static int portNumber = 2000;

    static String address;
    static String balance;

    static String version;
    static String uptime;
    static String nodes;
    static String staking;
    static String sync;

    static Socket qrlSocket;
    static PrintWriter out;
    static BufferedReader in;

    public static String getAddress() {
        return address;
    }

    public static String getBalance() {
        return balance;
    }

    public static String getVersion() {
        return version;
    }

    public static String getUptime() {
        double timed = Double.parseDouble(uptime);
        int time = (int) timed;
        
        int hours = time / 3600;
        int secondsLeft = time - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String formattedTime = "";

        if(hours != 0) {
            formattedTime += hours + " hrs ";
        } 
        
        if(minutes != 0) {
            formattedTime += minutes + " mins ";
        }
        
        if(seconds !=0) {
            formattedTime += seconds + " seconds";
        }

        return formattedTime;
    }

    public static String getNodes() {
        return nodes;
    }

    public static String getStaking() {
        return staking;
    }

    public static String getSync() {
        return sync;
    }

    public static boolean getInfo() {
        String rawInfo = "";
        
        rawInfo = contactNode("getinfo");
        if(!rawInfo.contains("Version")) {
                return false;
            }

        String[] tempInfos = rawInfo.split(">>>");
        String infoss = "";
        for (String s : tempInfos) {
            String[] s2 = s.split(": ");
            try {
                infoss += s2[1];
                infoss += "&";
            } catch (IndexOutOfBoundsException e) {
            }
        }

        String[] infos = infoss.split("&");
        try {
            version = infos[0];
            uptime = infos[1];
            nodes = infos[2];
            staking = infos[3];
            sync = infos[4];
        } catch (Exception e) {

        }
        return true;
    }

    public static boolean getWalletBalance() {
        String rawBalance = "";

        rawBalance = contactNode("wallet");
            if(!rawBalance.contains("balance")) {
                return false;
            }

        if (rawBalance != "") {
            try {
                String walletAddress = rawBalance.substring((rawBalance.indexOf("'") + 1), rawBalance.indexOf("'", rawBalance.indexOf("'") + 1));
                String walletBalance = rawBalance.substring(rawBalance.indexOf("balance") + 9, rawBalance.indexOf("("));

                address = walletAddress;
                balance = walletBalance;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String[] sendQRL(String fromAddress, String toAddress, String amount) {
        String query = "send " + fromAddress + " " + toAddress + " " + amount;
        String sendResponse = "";
        sendResponse = contactNode(query);
        String[] responses = sendResponse.split(">>>");

        return responses;
    }

    public static void connect() {
        try {
            qrlSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(qrlSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(qrlSocket.getInputStream()));
        } catch (Exception e) {

        }
    }

    public static String contactNode(String command) {
        String response = "";
        try {
            qrlSocket.setSoTimeout(25);
            out.println(command);

            while (!in.ready()) {
            }

            String line = "";
            while (line != null) {
                response = response + line;
                line = in.readLine();
            }
            out.flush();
            in.reset();
            qrlSocket.close();
        } catch (IOException e) {
        }
        return response;
    }
}
