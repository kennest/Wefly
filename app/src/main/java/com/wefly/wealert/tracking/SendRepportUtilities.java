package com.wefly.wealert.tracking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by root on 12/2/17.
 */

public class SendRepportUtilities {

    public static String getResponseFromHttpUrl(String repport, String Base_url) throws IOException {

        try {
            DataOutputStream printout;
            DataInputStream input;
            URL url = new URL(Base_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json");
//            conn.setRequestProperty("Authorization","Bearer " + token);
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setDoOutput(true);
            conn.connect();
            printout = new DataOutputStream(conn.getOutputStream ());
            printout.writeBytes(repport);
            printout.flush ();
            printout.close ();

            int responsecode = conn.getResponseCode();
            if (responsecode != 200){
                return "erreur";
            }
            InputStream in = conn.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return "erreur";
            }

        }catch(Exception e){
            e.printStackTrace();
            return "false";
        }
    }
}
