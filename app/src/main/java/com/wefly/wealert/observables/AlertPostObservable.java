package com.wefly.wealert.observables;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.appizona.yehiahd.fastsave.FastSave;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.wefly.wealert.tasks.AlertPostItemTask.JSON;

public class AlertPostObservable {
    AppController appController=AppController.getInstance();
    static final String url= Constants.SEND_ALERT_URL;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    final Alert alert = new Alert();
    private String response;

    @SuppressLint("CheckResult")

    public Observable<Boolean> send(Alert alert){
       return Observable.fromCallable(() -> process(alert));
    }

    private Boolean process(Alert alert){
        try {
            response = getResponseFromHttpUrl(Constants.SEND_ALERT_URL,alert);
            Log.e("Alert Sent Response ", response.trim());

            //Store the response in the sharedPref
            SharedPreferences sp = appController.getSharedPreferences("sent_data", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("sent_response", response);
            editor.apply();

        } catch (Exception e) {
            EventBus.getDefault().post(new JsonExceptionEvent("Error:"+e.getMessage()));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getResponseFromHttpUrl(@NonNull String url,@NonNull Alert alert) throws IOException {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        //Populate the json parameters
        try {
            json.put("titre", alert.getObject());
            json.put("contenu", alert.getContent());
            json.put("destinataires", recipientsIDFromPrefs());

            json.put("longitude", Double.valueOf(FastSave.getInstance().getString("long")));
            json.put("latitude", Double.valueOf(FastSave.getInstance().getString("lat")));

            json.put("date_alerte", new org.joda.time.DateTime(org.joda.time.DateTimeZone.UTC));


            //on revoie l'Id de la categorie correspondant au texte contenu dans la categorie
            for (Map.Entry entry : appController.alert_categories.entrySet()) {
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
                .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken())
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String recipientsIDFromPrefs() {
        //We retrieve the recipients ID store in RecipientAdapter an attach them to alert object
        SharedPreferences sp = appController.getApplicationContext().getSharedPreferences("recipients", 0);
        Set recipient_ids = sp.getStringSet("recipients_id", new HashSet<String>());
        List<String> list = new ArrayList<String>(recipient_ids);
        String objects = list.toString();
        return objects;
    }
}
