package com.wefly.wealert.services;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;

import com.wefly.wealert.events.JsonExceptionEvent;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.wefly.wealert.tasks.AlertPostItemTask.JSON;

public class SendAlertProcess {

    AppController app=AppController.getInstance();
    static final String url=Constants.SEND_ALERT_URL;

    @SuppressLint("CheckResult")

    private void send(Alert alert){
        Observable.fromCallable(() -> {
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            //Populate the json parameters
            try {
                json.put("titre", alert.getObject());
                json.put("contenu", alert.getContent());
                json.put("destinataires", recipientsIDFromPrefs());
                json.put("longitude", Double.valueOf(app.longitude));
                json.put("latitude", Double.valueOf(app.latitude));
                json.put("date_alerte", new org.joda.time.DateTime(org.joda.time.DateTimeZone.UTC));

                //on revoie l'Id de la categorie correspondant au texte contenu dans la categorie
                for (Map.Entry entry : app.alert_categories.entrySet()) {
                    if (entry.getKey().toString().equals(alert.getCategory()))
                        Log.v("Alert CAT", entry.getKey().toString() + ":" + alert.getCategory());
                    json.put("categorie", entry.getValue());
                }
                Log.v("ALERT JSON PARAMS", json.toString());
            } catch (JSONException e) {
                EventBus.getDefault().post(new JsonExceptionEvent("Error:"+e.getMessage()));
                e.printStackTrace();
            }
            //Create the request
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + app.getToken())
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                Log.e("Network request", "Failure", e);
            }

            return false;
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    //Use result for something
                    Log.v("Result",result.toString());
                });
    }

    public String recipientsIDFromPrefs() {
        //We retrieve the recipients ID store in RecipientAdapter an attach them to alert object
        SharedPreferences sp = app.getApplicationContext().getSharedPreferences("recipients", 0);
        Set recipient_ids = sp.getStringSet("recipients_id", new HashSet<String>());
        List<String> list = new ArrayList<String>(recipient_ids);
        String objects = list.toString();
        return objects;
    }
}
