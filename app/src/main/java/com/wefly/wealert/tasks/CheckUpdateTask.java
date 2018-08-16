package com.wefly.wealert.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wefly.wealert.presenters.DBActivity;
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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by admin on 02/04/2018.
 */

public class CheckUpdateTask extends TaskPresenter {
    String response = "";
    private DBActivity act;
    private OnUpdateCheckCompleteListener listener;
    private String TAG = getClass().getSimpleName();
    private boolean isStop;
    private boolean isForUp;
    private AppController appController;

    public CheckUpdateTask(@NonNull DBActivity activity, boolean isForUpdate) {
        this.act = activity;
        isForUp = isForUpdate;
        appController = AppController.getInstance();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            SignInTaskNetworkUtilities util = new SignInTaskNetworkUtilities();
            //response = util.getResponseFromHttpUrl(Constants.CONSTANTS_UPDATE_URL);
            Log.v(Constants.APP_NAME, TAG + "response" + response);
            if (!response.trim().equals("") && !response.trim().equals(Constants.SERVER_ERROR) && !response.trim().contains(Constants.RESPONSE_ERROR_HTML))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isOk) {
        super.onPostExecute(isOk);
        notifyOnUpdateCheckCompleteListener(isOk, response);

    }

    public void setOnUpdateCheckCompleteListener(@NonNull OnUpdateCheckCompleteListener listener) {
        this.listener = listener;
    }

    private void notifyOnUpdateCheckCompleteListener(boolean isOK, @NonNull String response) {
        if (listener != null) {
            try {
                if (isOK) {
                    JSONObject obj = new JSONObject(response);
                    listener.onDownloadSucces(obj);
                } else {
                    listener.onDownloadError(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnUpdateCheckCompleteListener {
        void onDownloadError(@NonNull String errorMsg);

        void onDownloadSucces(@NonNull JSONObject response);
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
            Log.v(Constants.APP_NAME, TAG + "doInBackGround url " + base_url);

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
                Log.v(Constants.APP_NAME, TAG + " doInBackGround toString response ");
                return builder.toString();
            } catch (UnsupportedEncodingException e) {
                Log.v(Constants.APP_NAME, TAG + " doInBackGround UnsupportedEncodingException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.v(Constants.APP_NAME, TAG + " IOException");
                e.printStackTrace();
            }
            return Constants.SERVER_ERROR;
        }

    }
}
