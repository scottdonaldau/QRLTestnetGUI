package QRLTestnetWallet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aidan
 */
public class ContactNode {

    static String hostName = "127.0.0.1";
    static int portNumber = 2000;

    static Socket qrlSocket;
    static PrintWriter out;
    static ByteArrayOutputStream baos;
    static InputStream is;

    public void connect() {
        try {
            qrlSocket = new Socket(hostName, portNumber);

            out = new PrintWriter(qrlSocket.getOutputStream(), true);
            baos = new ByteArrayOutputStream();
            is = qrlSocket.getInputStream();
        } catch (IOException ex) {
            System.out.println("ERROR CONNECTING TO NODE");
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("ANOTHER ERROR");
            e.printStackTrace();
        }
    }

    public JsonObject sendCommandAndGetJsonObject(String command, int timeout) {
        String stringResponse = "";

        stringResponse = sendCommandAndGetString(command, timeout);

        JsonObject objectResponse = null;
        try {
            objectResponse = Json.parse(stringResponse).asObject();
        } catch (ParseException pe) {
            System.out.println("Could not parse reponse to JsonObject...");
        }

        return objectResponse;
    }
    
     public JsonObject sendCommandAndGetJsonObject(String command, String nestedContent, int timeout) {
        String stringResponse = "";

        stringResponse = sendCommandAndGetString(command, timeout);

        JsonObject objectResponse = null;
        JsonObject objectResponseNested = null;
        try {
            objectResponse = Json.parse(stringResponse).asObject();
            objectResponseNested = objectResponse.get(nestedContent).asObject();
        } catch (ParseException pe) {
            //pe.printStackTrace();
            System.out.println("Could not parse reponse to JsonObject...");
        }

        return objectResponseNested;
    }

    public JsonArray sendCommandAndGetJsonArray(String command, String nestedContent, int timeout) {
        String stringResponse = sendCommandAndGetString(command, timeout);

        JsonValue jsonResponse = null;

        try {
            if (nestedContent != null) {
                jsonResponse = Json.parse(stringResponse);
                JsonObject objectResponse = jsonResponse.asObject();
                JsonArray arrayResponse = objectResponse.get(nestedContent).asArray();
                return arrayResponse;
            } else {
                jsonResponse = Json.parse(stringResponse);
                JsonArray arrayResponse = jsonResponse.asArray();
                return arrayResponse;
            }
        } catch (ParseException pe) {
            System.out.println("Could not parse reponse to JsonArray...");
        } catch (Exception e) {
            System.out.println("Could not parse reponse to JsonArray...");
        }
        return null;
    }

    private String sendCommandAndGetString(String command, int timeout) {
        try {
            System.out.println("SERVER SENDING COMMAND...: " + command);
            byte[] byteResponse = sendCommandAndGetBytes(command, timeout);

            String stringResponse = new String(byteResponse);
            return stringResponse;
        } catch (Exception e) {
            System.out.println("Could not parse reponse to String...");
        }
        return null;
    }

    private byte[] sendCommandAndGetBytes(String command, int timeout) {
        try {
            byte[] byteResponse = retrieve(command, timeout);
            return byteResponse;
        } catch (Exception e) {
            //Error Message
        }
        return null;
    }

    public static byte[] retrieve(String command, int timeout) {
        try {
            
            baos.close();
            qrlSocket.setSoTimeout(timeout);
            out.println(command);
            baos.reset();
            byte[] buffer = new byte[1024];
            int readBytes;
            try {
                while ((readBytes = is.read(buffer)) > 0) {
                    String tostringstuff = new String(buffer);
                    baos.write(buffer, 0, readBytes);
                }
            } catch (IOException e) {
                byte[] responseArray = baos.toByteArray();
                baos.reset();

                //String tostringstuff = new String(responseArray);
                //System.out.println("Node Response: " + tostringstuff);
                return responseArray;
            }
            baos.reset();
        } catch (Exception e) {
            baos.reset();
            System.out.println("Unable to send/receive message...");
            e.printStackTrace();
            return null;
        }
        baos.reset();
        return null;
    }
}
