package com.wefly.wealert.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlertReceiveGetTask extends AsyncTask<String, Integer, List<Alert>> {
    private AppController appController;
    private String result;
    private List<Alert> receiveAlerts = new ArrayList<>();
    Integer count;
    String next;

    public AlertReceiveGetTask(AppController appController) {
        this.appController = appController;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Alert> doInBackground(String... strings) {
        try {
            JSONObject resultJSON;
            result = getResponseFromUrl(Constants.ALERT_RECEIVE_URL);
            try {
                resultJSON = new JSONObject(result);
                count = Integer.parseInt(resultJSON.getString("count"));

                //On stocke le resultat dans les sharedprefs pour affichache du card View
                SharedPreferences sp = appController.getSharedPreferences("alert_receive_data", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("alert_responses", resultJSON.toString());
                editor.apply();
                Log.v("results array",sp.getString("alert_responses","ARRAY VIDE"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(count>0)
                receiveAlerts.addAll(Objects.requireNonNull(extractAlert(result)));

            //Si on a encore d'autres page suivante on extrait les emails et on les ajoutes au emails recus
            try {
                resultJSON = new JSONObject(result);
                int i = 1;
                count = Integer.parseInt(resultJSON.getString("count"));
                next = resultJSON.getString("next");
                if(count>0 && !next.equals("null"))
                    do {
                        receiveAlerts.addAll(Objects.requireNonNull(extractAlert(getResponseFromUrl(next))));
                        resultJSON = new JSONObject(getResponseFromUrl(next));
                        next = resultJSON.getString("next");
                        Log.v("Next value" + i, String.valueOf(i));
                    } while (++i == (count-1));

                Log.v("Alert receive count " + i, String.valueOf(receiveAlerts.size()));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.v("Alert Receive Response", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiveAlerts;
    }

    //Fonction de recuperation des infos sur le serveur
    private String getResponseFromUrl(@NonNull String url) throws IOException {
        Response response;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken())
                .build();

        response = client.newCall(request).execute();

        return response.body().string();
    }

    //Fonction d'extraction des emails du flux JSON
    private List<Alert> extractAlert(@NonNull String json) {
        List<Alert> extractedList = new ArrayList<>();
        //On recupere les emails du flux JSON
        try {
            JSONObject resultJSON = new JSONObject(json);
            JSONArray emailsArray = resultJSON.getJSONArray("results");

            JSONObject emailReceive = emailsArray.getJSONObject(0);

            String expediteur = emailReceive.getJSONObject("alerte").getJSONObject("creer_par").getJSONObject("user").getString("username");

            Alert alert = new Alert();


            alert.setContent(emailReceive.getJSONObject("alerte").getString("contenu"));
            alert.setDateCreated(emailReceive.getJSONObject("alerte").getString("date_de_creation"));
            alert.setObject(emailReceive.getJSONObject("alerte").getString("titre"));
            alert.setRecipientsString(expediteur);

            extractedList.add(alert);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return extractedList;
    }
}
