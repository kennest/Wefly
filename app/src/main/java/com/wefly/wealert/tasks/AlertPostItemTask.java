package com.wefly.wealert.tasks;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;
import com.wefly.wealert.events.AlertSentEvent;
import com.wefly.wealert.events.BeforeSendAlertEvent;
import com.wefly.wealert.events.JsonExceptionEvent;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.presenters.TaskPresenter;
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

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by admin on 02/04/2018.
 */

public class AlertPostItemTask extends TaskPresenter implements PieceUploadTask.OnPieceSendListener {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    final Alert alert;
    private String response;
    private Set<Piece> pieces;
    private OnAlertSendListener listener;
    private String TAG = getClass().getSimpleName();
    private AppController appController = AppController.getInstance();
    PieceUploadTask.OnPieceSendListener uploadlistener;
    CircularProgressIndicator bar;

    public void setProgressBar(CircularProgressIndicator bar) {
        this.bar = bar;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.e(getClass().getSimpleName(), "Progress" + String.valueOf(values[0]));
        if (this.bar != null) {
            bar.setCurrentProgress(values[0]);
        }
    }

    public AlertPostItemTask(@NonNull Alert alert) {
        this.alert = alert;
        appController = AppController.getInstance();
        //this.pieces = appController.getPieceList();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EventBus.getDefault().post(new BeforeSendAlertEvent("Alert is being sended"));
        //FancyToast.makeText(AppController.getInstance().getApplicationContext(), "Envoi de l'alert...", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        AlertPostItemNetworkUtilities util = new AlertPostItemNetworkUtilities();
        try {
            Thread.sleep(1000);
            publishProgress(1);
            response = util.getResponseFromHttpUrl(Constants.SEND_ALERT_URL);
            Log.v("Alert Sent Response ", response.trim());

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

    @Override
    protected void onPostExecute(Boolean isOk) {
        super.onPostExecute(isOk);
        bar.setCurrentProgress(0);
        setProgressBar(null);
        EventBus.getDefault().post(new AlertSentEvent("Alert Successfully sent"));
    }

    public void setOnAlertSendListener(@NonNull OnAlertSendListener listener) {
        this.listener = listener;
    }

    private void notifyOnAlertSendListener(boolean isDone, @NonNull Alert alert) {
        if (listener != null) {
            try {
                if (isDone)
                    listener.onSendSucces(alert);
                else
                    listener.onSendError(alert);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnPieceSendListener(@NonNull PieceUploadTask.OnPieceSendListener listener) {
        this.uploadlistener = listener;
    }

    private void notifyOnPieceSendListener(boolean isDone) {
        if (uploadlistener != null) {
            try {
                if (isDone)
                    uploadlistener.onUploadSucces();
                else
                    uploadlistener.onUploadError();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUploadError() {

    }

    @Override
    public void onUploadSucces() {

    }

    public interface OnAlertSendListener {
        void onSendError(@NonNull Alert e);

        void onSendSucces(@NonNull Alert e);
    }

    public final class AlertPostItemNetworkUtilities {
        public String getResponseFromHttpUrl(@NonNull String url) throws IOException {
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();

            //Populate the json parameters
            try {
                json.put("titre", alert.getObject());
                json.put("contenu", alert.getContent());
                json.put("destinataires", recipientsIDFromPrefs());
                json.put("longitude", Double.valueOf(appController.longitude));
                json.put("latitude", Double.valueOf(appController.latitude));
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
                notifyOnAlertSendListener(false, alert);
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
}
