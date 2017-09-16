package Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;

/**
 *
 * @author Aidan
 */
public class MarketData {

    private String QRLUSDPrice;
    private String QRLBTCPrice;
    private String QRLRank;

    public Boolean collectQRLData() throws Exception {
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

            JsonObject objectResponse = null;
            try {
                String QRLTemp = response.toString().substring(1, response.toString().length() - 1);
                objectResponse = Json.parse(QRLTemp.toString()).asObject();

                QRLUSDPrice = objectResponse.getString("price_usd", "Unavailable");
                QRLBTCPrice = objectResponse.getString("price_btc", "Unavailable");
                QRLRank = objectResponse.getString("rank", "Unavailable");
                
            } catch (ParseException pe) {
                System.out.println("Could not parse market data reponse to JsonObject...");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getQRLUSDPrice() {
        return QRLUSDPrice;
    }

    public String getQRLBTCPrice() {
        return QRLBTCPrice;
    }

    public String getQRLRank() {
        return QRLRank;
    }
}
