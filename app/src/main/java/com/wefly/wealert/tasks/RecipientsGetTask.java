package com.wefly.wealert.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.presenters.BaseActivity;
import com.wefly.wealert.presenters.TaskPresenter;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by admin on 02/04/2018.
 */

public class RecipientsGetTask extends TaskPresenter {
    String response = "";
    private BaseActivity act;
    private AppController appController;
    private OnRecipientsDownloadCompleteListener listener;
    private boolean hasPrev, hasNext;
    private String prev = "", next = "";
    private String url = "";
    private int size;
    private JSONArray array = null;
    private String TAG = getClass().getSimpleName();

    public RecipientsGetTask(@NonNull BaseActivity activity, @NonNull String url) {
        this.act = activity;
        this.url = url;
        appController = AppController.getInstance();
        this.size = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            SignInTaskNetworkUtilities util = new SignInTaskNetworkUtilities();
            response = util.getResponseFromHttpUrl(url);
            Log.v(Constants.APP_NAME, TAG + " Recipients get response " + response);
            if (!response.trim().equals("") && !response.trim().equals(Constants.SERVER_ERROR) && !response.trim().contains(Constants.RESPONSE_ERROR_HTML)) {
                if (response.trim().equals(Constants.RESPONSE_EMPTY)) {
                    // List Empty
                    return false;
                }

                // Everything is Ok
                JSONObject object = new JSONObject(response);

                size = object.getInt("count");
                hasNext = !(object
                        .getString("next").equals("null"));
                if (hasNext)
                    next = object.getString("next");
                hasPrev = !(object
                        .getString("previous").equals("null"));
                if (hasPrev)
                    prev = object.getString("previous");
                array = object
                        .getJSONArray("results");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isOk) {
        super.onPostExecute(isOk);

        if (appController != null && array != null) {
            Log.v(Constants.APP_NAME, TAG + " onPostExecute RUN");
            notifyOnRecipientsDownloadCompleteListener(isOk, appController.recipiencesJSONArrToList(array), hasPrev, hasNext, prev, next, size);
        }
    }

    public void setOnRecipientsDownloadCompleteListener(@NonNull OnRecipientsDownloadCompleteListener listener) {
        this.listener = listener;
    }

    private void notifyOnRecipientsDownloadCompleteListener(boolean isOK, @NonNull CopyOnWriteArrayList<Recipient> list, boolean hPrev, boolean hNext, @NonNull String prev, @NonNull String next, int max) {
        if (listener != null) {
            try {
                Log.v(Constants.APP_NAME, TAG + " notifyOnRecipientsDownloadCompleteListener RUN");
                if (isOK) {
                    listener.onDownloadSucces(list, hPrev, hNext, prev, next, max);
                } else {
                    listener.onDownloadError(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnRecipientsDownloadCompleteListener {
        void onDownloadError(@NonNull String errorMsg);

        void onDownloadSucces(@NonNull CopyOnWriteArrayList<Recipient> recipentsArray, boolean hPrev, boolean hNext, @NonNull String prev, @NonNull String next, int max);
    }

    public final class SignInTaskNetworkUtilities {
        public String getResponseFromHttpUrl(@Nullable String base_url) {

            HttpClient httpclient;
            HttpGet httpget = new HttpGet(base_url);
            HttpResponse response;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.VOLLEY_TIME_OUT);
            HttpConnectionParams.setSoTimeout(httpParameters, Constants.VOLLEY_TIME_OUT);
            httpclient = new DefaultHttpClient(httpParameters);

            try {
                httpget.setHeader("Content-type", "application/json;charset=UTF-8");
                httpget.setHeader("Accept-Type", "application/json");
                if (appController != null)
                    httpget.setHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken());
                response = httpclient.execute(httpget);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }
                return builder.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Constants.SERVER_ERROR;
        }

    }
}
