package com.wefly.wealert.observables;

import android.support.annotation.NonNull;
import android.util.Log;

import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecipientsListObservable {
    private String response = "";
    private String next = "";
    Integer count = 0;
    private AppController appController = AppController.getInstance();
    List<Recipient> recipientList = new ArrayList<>();

    public Observable<Recipient> getList() {
        return Observable.create(e -> {
            //Call network request
            recipientList.addAll(process());
            for (Recipient r:recipientList) {
                e.onNext(r);
            }
            e.onComplete();
        });
    }

    private List<Recipient> process() {
        Log.v("init recipients get new", "started");
        JSONObject resultJSON;
        List<Recipient> recipients = new ArrayList<>();
        try {
            response = getRecipientsFromUrl(Constants.RECIPIENTS_URL);
            resultJSON = new JSONObject(response);
            count = Integer.parseInt(resultJSON.getString("count"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (count > 0)
            recipients.addAll(extractRecipients(response));

        //Si on a encore des pages suivantes on lance de nouveau la requete
        try {
            int i = 1;
            resultJSON = new JSONObject(response);
            count = Integer.parseInt(resultJSON.getString("count"));
            next = resultJSON.getString("next");
            if (count > 0 && !next.equals("null"))
                do {
                    recipients.addAll(extractRecipients(getRecipientsFromUrl(next)));
                } while (++i == (count - 1));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Log.v("Task time", String.valueOf(recipients.size()));
        appController.setRecipients(recipients);
        return recipients;
    }

    private String getRecipientsFromUrl(@NonNull String url) throws IOException {

        OkHttpClient client = new OkHttpClient();
        String result;

        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken())
                .build();
        Response response = client.newCall(request).execute();

        result = response.body().string();
        return result;
    }


    //Fonction d'extraction des categories du flux JSON
    private List<Recipient> extractRecipients(@NonNull String json) {
        List<Recipient> extractedRecipents = new ArrayList<>();
        try {
            JSONObject response_data = new JSONObject(json);
            JSONArray response_recipients = new JSONArray(response_data.getString("results"));

            for (int i = 0; i < response_recipients.length(); i++) {
                Recipient recipient = new Recipient();
                JSONObject item = new JSONObject(response_recipients.getString(i));
                JSONObject user = new JSONObject(item.getString("user"));

                recipient.setRecipientId(item.getInt("id"));
                recipient.setEmail(user.getString("email"));
                recipient.setLastName(user.getString("last_name"));
                recipient.setUserName(user.getString("last_name"));
                recipient.setFirstName(user.getString("first_name"));
                recipient.setDateCreate(item.getString("create_at"));
                recipient.setFonction(item.getJSONObject("fonction").getInt("id"));
                recipient.setEntreprise(item.getJSONObject("fonction").getInt("entreprise"));
                recipient.setAdresse(item.getInt("adresse"));
                recipient.setAvatarUrl(item.getString("photo"));
                extractedRecipents.add(recipient);
            }

            Log.v("RECIPIENTS LIST", response_recipients.toString());

            for (Recipient p : extractedRecipents) {
                Log.v("Entry username" + extractedRecipents.indexOf(p), p.getLastName());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return extractedRecipents;
    }
}
