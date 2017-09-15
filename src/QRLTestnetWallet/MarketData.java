/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QRLTestnetWallet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

/**
 *
 * @author Aidan
 */
public class MarketData {

    public static String collectQRLData() throws Exception {
        try {
            String url = "https://api.coinmarketcap.com/v1/ticker/quantum-resistant-ledger/?convert=USD";

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String QRLPrice = null;
            JsonObject objectResponse = null;
            try {
                String QRLTemp = response.toString().substring(1, response.toString().length() - 1);
                objectResponse = Json.parse(QRLTemp.toString()).asObject();

                QRLPrice = objectResponse.getString("price_usd", "Unavailable");
            } catch (ParseException pe) {
                System.out.println("Could not parse reponse to JsonObject...");
            }

            return QRLPrice;

            //System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
    public static void main(String[] args) {
        try {
            collectData();
        } catch (Exception ex) {
            Logger.getLogger(MarketData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     */
}
